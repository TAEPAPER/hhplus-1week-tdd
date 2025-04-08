package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PointIntegrationTest {
    /**
     * 통합 테스트 : 상호작용 확인이 필요할 때 하는 테스트
     */
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    PointService pointService;

    @Autowired
    UserPointTable userPointTable;

    @Autowired
    PointHistoryTable pointHistoryTable;

    //Controller 테스트
    @Test
    void PointController_테스트() throws Exception {
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk());
    }

    /**
     * 포인트 조회 성공 테스트
     * @throws Exception
     */
    @Test
    void 포인트_조회_성공() throws Exception {
        //given
        long userId = 1L;

        //when
        mockMvc.perform(get("/point/1"))
                .andExpect(status().isOk());

        //then
        UserPoint userPoint = userPointTable.selectById(1L);
        assertEquals(0L, userPoint.point());
    }

    /**
     * 포인트 충전/사용 내역 통합 테스트
     * @throws Exception
     */
    @Test
    void 포인트_충전_사용_내역_통합_테스트() throws Exception {
        //given
        long userId = 1L;

        //when
        mockMvc.perform(get("/point/1/histories"))
                .andExpect(status().isOk());

        List<PointHistory> pointHistoryTableList = pointHistoryTable.selectAllByUserId(1L);
        assertEquals(List.of(), pointHistoryTableList);
    }

    /**
     * 포인트 충전 통합 테스트
     * @throws Exception
     */
    @Test
    void 포인트_충전하면_유저_포인트_테이블과_이력_테이블_조회_가능() throws Exception {
        //given
        long userId = 1L;
        long amount = 1000L;

        // when
        mockMvc.perform(patch("/point/" + userId + "/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(amount)))
                .andExpect(status().isOk());

        // then
        UserPoint userPoint = userPointTable.selectById(userId);
        assertEquals(1000L, userPoint.point());

        List<PointHistory> pointHistoryList = pointHistoryTable.selectAllByUserId(userId);
        assertEquals(1, pointHistoryList.size());
        assertEquals(amount, pointHistoryList.get(0).amount());
        assertEquals(TransactionType.CHARGE, pointHistoryList.get(0).type());
    }

    /**
     * 포인트 사용 통합 테스트
     * @throws Exception
     */
    @Test
    void 포인트_충전_후_사용하면_유저_포인트_테이블과_이력_테이블_조회_가능() throws Exception {

        //given
        long userId = 1L;
        long chargeAmount = 1000L;
        long useAmount = 500L;

        // when
        mockMvc.perform(patch("/point/" + userId + "/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(chargeAmount)))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/point/" + userId + "/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(useAmount)))
                .andExpect(status().isOk());

        //then
        UserPoint userPoint = userPointTable.selectById(userId);
        List<PointHistory> pointHistoryList = pointHistoryTable.selectAllByUserId(userId);

        assertEquals(500L, userPoint.point());
        assertEquals(2, pointHistoryList.size());
        assertEquals(500L, pointHistoryList.get(1).amount());
        assertEquals(TransactionType.USE, pointHistoryList.get(1).type());

    }

    /**
     * 포인트 사용 동시성 테스트
     * @throws Exception
     */
    @Test
    void 포인트_충전_후_동일한_사용자_포인트_사용_동시_요청() throws Exception {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        //포인트 충전
        mockMvc.perform(patch("/point/1/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(10000L)))
                .andExpect(status().isOk());
        //then
        UserPoint userPoint = userPointTable.selectById(1L);
        assertEquals(10000L, userPoint.point());

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.use(1L, 2000);
                } catch (IllegalArgumentException e) {
                    System.out.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 쓰레드가 종료될 때까지 대기

        List<PointHistory> list = pointHistoryTable.selectAllByUserId(1L);
        long expectedPoints = 10000L - (2000L * (list.size() - 1 ));

        assertEquals(expectedPoints, userPointTable.selectById(1L).point());

    }

    /**
     * 포인트 충전 동시성 테스트
     * @throws Exception
     */
    @Test
    void 동일한_사용자_포인트_충전_동시_요청() throws Exception {

        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //then
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    pointService.charge(1L,2000);
                }catch (IllegalArgumentException e) {
                    System.out.println("Exception: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await(); // 모든 쓰레드가 종료될 때까지 대기

        List<PointHistory> list = pointHistoryTable.selectAllByUserId(1L);
        long expectedPoints = (2000L * (list.size()));

        assertEquals(expectedPoints, userPointTable.selectById(1L).point());
    }
}