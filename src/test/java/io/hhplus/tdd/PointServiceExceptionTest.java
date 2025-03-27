package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import io.hhplus.tdd.point.validator.PointValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class PointServiceExceptionTest {

    private PointService pointService;

    @Mock
    private UserPointTable userPointTable;

    @Mock
    private PointHistoryTable pointHistoryTable;

    @Mock
    private PointValidator pointValidator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        pointValidator = new PointValidator();
        pointService = new PointService(userPointTable, pointHistoryTable, pointValidator);
    }

    private final static long ID = 1L;

    @Test
    void 포인트_충전_금액_0_보다_작으면_예외_발생(){
        //given
        long invalidAmount = 0;

        assertThrows(IllegalArgumentException.class, () -> {
            //when
            pointService.charge(ID, 0L);
        });
    }

    @Test
    void 포인트_사용_금액_0_보다_작으면_예외_발생() {
        //given
        long invalidAmount = 0;

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(ID, invalidAmount);
        });
    }

    @Test
    void 포인트_사용_금액_보유_금액_보다_크면_예외_발생() {
        //given
        long point = 1000L;
        long invalidAmount = 2000L;

        when(userPointTable.selectById(ID)).thenReturn(new UserPoint(ID, point, 123L));

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.use(ID, invalidAmount);
        });
    }
    @Test
    void 사용자_없을_시_예외_발생() {
        //given
        long invalidUserId = 0L;

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.selectPoint(invalidUserId);
        });
    }
    //최대 포인트 한도
    @Test
    void 충전_포인트_한도_초과_시_예외_발생() {
        //given
        long point = 1000000L;
        long amount = 1L;

        when(userPointTable.selectById(ID)).thenReturn(new UserPoint(ID, point, anyLong()));

        assertThrows(IllegalArgumentException.class, () -> {
            pointService.charge(ID, amount);
        });
    }

}
