package com.webperformance.muse.measurements;

import org.junit.*;
import org.musetest.core.*;
import org.musetest.core.events.*;
import org.musetest.core.step.*;
import org.musetest.core.tests.mocks.*;

/**
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
public class BasicCollectorTests
    {
    @Test
    public void collectSingleStepDuration()
        {
        StepConfiguration step_config = new StepConfiguration("mock-step");
        final MockStepExecutionContext context = new MockStepExecutionContext();
        StepEvent start_event = new StepEvent(MuseEventType.StartStep, step_config, context);
        MockStepEvent end_event = new MockStepEvent(MuseEventType.EndStep, step_config, context);
        end_event.setTimestampNanos(start_event.getTimestampNanos() + 1000);

        // create a collector
        StepDurationCollector collector = new StepDurationCollector();
        collector.initialize(context);
        context.raiseEvent(start_event);
        context.raiseEvent(end_event);

        Assert.assertEquals(1, collector.getMeasurements().size());
        Assert.assertEquals(1000L, collector.getMeasurements().get(0).getValue().longValue());
        }
    }


