package com.alexandervanderzalm.game.Model;

import org.junit.jupiter.api.Test;
import org.springframework.util.Assert;

import static org.junit.jupiter.api.Assertions.*;

class ObservedPitTest {

    private Integer ChangeLogger = 0;

    @Test
    void add_AddedEvent_FiredEventOnChange() {
        // Setup
        ObservedPit p = new ObservedPit();
        p.OnChangedHelper.AddOnChangedProcedure(() -> ChangeLogger +=1 );
        ChangeLogger = 0;

        // Test Basic functionality
        p.Add(2);
        Assert.isTrue(p.Amount() == 2, "AddedTwoStones_PitAmountIsTwo");

        // Test events triggered
        Assert.isTrue(ChangeLogger== 1, "CheckedIfEventTriggeredOnce");
    }

    @Test
    void grabAll_AddedEvent_FiredEventOnChange() {
        // Setup
        ObservedPit p = new ObservedPit();
        p.OnChangedHelper.AddOnChangedProcedure(() -> ChangeLogger +=1 );
        ChangeLogger = 0;

        // Test Basic functionality
        p.Add(2);
        Assert.isTrue(p.Amount() == 2, "AddedTwoStones_PitAmountIsTwo");
        Integer hand = p.GrabAll();
        Assert.isTrue(p.Amount() == 0, "GrabbedAllStones_PitAmountIsZero");
        Assert.isTrue(hand == 2, "GrabbedAllStones_HandHasTwoStones");

        // Test events triggered
        Assert.isTrue(ChangeLogger== 2, "CheckedIfEventTriggeredTwoTimes");
    }
}