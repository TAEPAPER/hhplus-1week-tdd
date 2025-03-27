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

    /**
     * 특정 유저의 포인트를 조회하는 기능
     * @param userId
     * @return 조회한 UserPoint
     */
    public UserPoint selectPoint(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);
        pointValidator.validateUserPoint(userPoint);

        return userPoint;
    }

    /**
     * 특정 유저의 포인트 충전/이용 내역을 조회하는 기능
     * @param userId
     * @return 조회한 List<PointHistory>
     */
    public List<PointHistory> selectPointHistory(long userId) {

        UserPoint userPoint = userPointTable.selectById(userId);
        pointValidator.validateUserPoint(userPoint);

        return pointHistoryTable.selectAllByUserId(userId);
    }

    /**
     * 특정 유저의 포인트를 충전하는 기능
     * @param userId
     * @param amount
     * @return 충전 UserPoint
     */
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

    /**
     * 특정 유저의 포인트를 사용하는 기능
     * @param userId
     * @param amount
     * @return 사용 UserPoint
     */
    public  UserPoint use(long userId, long amount) {

            UserPoint userPoint = userPointTable.selectById(userId);
            pointValidator.validateUse(userPoint, amount);

            userPointTable.insertOrUpdate(userId, userPoint.point() - amount);

            long currentTime = System.currentTimeMillis();
            pointHistoryTable.insert(userId, amount, TransactionType.USE, currentTime);
            return new UserPoint(userId, amount, currentTime);
    }
}
