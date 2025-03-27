package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.validator.PointValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
public class PointServiceTest {


    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private PointValidator pointValidator;


    @BeforeEach
    void setUp() {
        pointValidator = new PointValidator();
        pointService = new PointService(userPointTable, pointHistoryTable, pointValidator);
    }

    private final static long ID = 1L;

    @Test
    void 사용자_포인트_조회() {
        // given
        long userId = 1L;
        long point = 1000L;
        long updateMillis = System.currentTimeMillis();
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, point, updateMillis));
        UserPoint expected = new UserPoint(userId, point, updateMillis);

        // when
        UserPoint actual = pointService.selectPoint(userId);

        // then
        assertEquals(expected, actual);
        verify(userPointTable).selectById(userId);
    }

    @Test
    void 사용자_포인트_충전_사용_내역_조회() {

        // given
        long userId = 1L;
        List<PointHistory> expectedList = List.of(
                new PointHistory(1L, userId, 1000L, TransactionType.CHARGE, 111L),
                new PointHistory(2L, userId, 500L, TransactionType.CHARGE, 222L),
                new PointHistory(3L, userId, 600L, TransactionType.USE, 333L)
        );
        when(userPointTable.selectById(eq(userId))).thenReturn(new UserPoint(1L,2000L,111L));
        when(pointHistoryTable.selectAllByUserId(eq(userId))).thenReturn(expectedList);

        // when
        List<PointHistory> actualList = pointService.selectPointHistory(userId);

        // then
        assertThat(actualList)
                .usingRecursiveComparison()
                .ignoringFields("updateMillis") // 시간 필드 무시
                .isEqualTo(expectedList);

        verify(userPointTable).selectById(eq(userId));
        verify(pointHistoryTable).selectAllByUserId(eq(userId));
    }

    @Test
    void 사용자_포인트_충전_후_히스토리_저장(){

        // given
        long userId = 1L;
        long initialAmount = 1000L;
        long chargeAmount = 2000L;
        long newAmount = initialAmount + chargeAmount;

        UserPoint initialUserPoint = new UserPoint(userId, initialAmount, 123L);
        UserPoint updatedUserPoint = new UserPoint(userId, newAmount, 123L);

        when(userPointTable.selectById(userId)).thenReturn(initialUserPoint);
        when(userPointTable.insertOrUpdate(userId, newAmount)).thenReturn(updatedUserPoint);
        when(pointHistoryTable.insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong()))
                .thenReturn(new PointHistory(1L, userId, chargeAmount, TransactionType.CHARGE, anyLong()));

        //when
        UserPoint actual = pointService.charge(userId, chargeAmount);

        // then
        assertEquals(newAmount, actual.point());
        verify(userPointTable).selectById(userId);
        verify(userPointTable).insertOrUpdate(userId, newAmount);
        verify(pointHistoryTable).insert(eq(userId), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong());
    }

    @Test
    void 사용자_포인트_사용_후_히스토리_저장() {

        // given
        when(userPointTable.selectById(eq(ID))).thenReturn(
                new UserPoint(ID, 2000L, 123L));

        when(userPointTable.insertOrUpdate(eq(ID), eq(1000L))).thenReturn(
                new UserPoint(ID, 1000L, 123L));

        when(pointHistoryTable.insert(eq(ID), eq(1000L), eq(TransactionType.USE), anyLong())).thenReturn(
                new PointHistory(1L, ID, 1000L, TransactionType.USE, 123L));

        // when
        UserPoint actual = pointService.use(ID, 1000L);

        // then
        assertEquals(1000L, actual.point());
        verify(userPointTable).selectById(eq(ID));
        verify(userPointTable).insertOrUpdate(eq(ID), eq(1000L));
        verify(pointHistoryTable).insert(eq(ID), eq(1000L), eq(TransactionType.USE), anyLong());
    }
}