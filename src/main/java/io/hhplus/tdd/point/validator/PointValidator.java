package io.hhplus.tdd.point.validator;

import io.hhplus.tdd.point.UserPoint;
import org.springframework.stereotype.Component;

@Component
public class PointValidator {

    private static final long MAX_BALANCE = 10000L;

    /**
     * 충전 금액 유효성 검사
     * @param amount
     */
    public void validateCharge(long amount) {
        System.out.println("validateCharge called with amount = " + amount);
        if (amount <= 0) {
            throw new IllegalArgumentException("충전 금액은 0보다 커야 합니다.");
        }
    }

    /**
     * 사용 금액 유효성 검사
     * @param userPoint
     * @param amount
     */
    public void validateUse(UserPoint userPoint, long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("사용 금액은 0보다 커야 합니다.");
        }
        if (userPoint.point() < amount) {
            throw new IllegalArgumentException("포인트가 부족합니다.");
        }
    }

    /**
     * 사용자 포인트 정보 유효성 검사
     * @param userPoint
     */
    public void validateUserPoint(UserPoint userPoint) {
        if (userPoint == null) {
            throw new IllegalArgumentException("사용자 포인트 정보가 없습니다.");
        }
    }

    /**
     * 최대 포인트 한도 유효성 검사
     * @param currentBalance
     * @param amount
     */
    public void validateMaxBalance(long currentBalance, long amount) {
        if (currentBalance + amount > MAX_BALANCE) {
            throw new IllegalArgumentException("포인트 최대 한도를 초과하였습니다.");
        }
    }
}
