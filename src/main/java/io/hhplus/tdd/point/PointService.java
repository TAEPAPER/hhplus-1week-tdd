package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.validator.PointValidator;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class PointService {

    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;
    private final PointValidator pointValidator;
    private final ReentrantLock lock = new ReentrantLock();

    public PointService(UserPointTable userPointTable, PointHistoryTable pointHistoryTable, PointValidator pointValidator) {
        this.userPointTable = userPointTable;
        this.pointHistoryTable = pointHistoryTable;
        this.pointValidator = pointValidator;
    }

    //포인트 조회
    public UserPoint selectPoint(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);
        pointValidator.validateUserPoint(userPoint);

        return userPoint;
    }

    // 포인트 충전/사용 내역 조회
    public List<PointHistory> selectPointHistory(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);
        pointValidator.validateUserPoint(userPoint);

        return pointHistoryTable.selectAllByUserId(userId);
    }

    //포인트 충전
    public UserPoint charge(long userId, long amount) {

            UserPoint userPoint = userPointTable.selectById(userId);
            pointValidator.validateCharge(amount);
            pointValidator.validateMaxBalance(userPoint.point(), amount);

            long newAmount = userPoint.point() + amount;
            userPointTable.insertOrUpdate(userId, newAmount);

            long currentTime = System.currentTimeMillis();
            pointHistoryTable.insert(userId, amount, TransactionType.CHARGE, currentTime);

            return new UserPoint(userId, newAmount, currentTime);
    }

    //포인트 사용
    public  UserPoint use(long userId, long amount) {

            UserPoint userPoint = userPointTable.selectById(userId);
            pointValidator.validateUse(userPoint, amount);

            userPointTable.insertOrUpdate(userId, userPoint.point() - amount);

            long currentTime = System.currentTimeMillis();
            pointHistoryTable.insert(userId, amount, TransactionType.USE, currentTime);
            return new UserPoint(userId, amount, currentTime);

    }
}
