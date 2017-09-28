package com.webperformance.muse.measurements;

import org.musetest.core.*;
import org.musetest.core.events.*;
import org.musetest.core.step.*;

import java.util.*;

/**
 * Collects performance measurements on steps tagged for collection.
 *
 * @author Christopher L Merrill (see LICENSE.txt for license details)
 */
public class StepDurationCollector implements DataCollector, MuseEventListener
    {
    @Override
    public void initialize(MuseExecutionContext context)
        {
        context.addEventListener(this);
        }

    public List<Measurement> getMeasurements()
        {
        return _measurements;
        }

    @Override
    public void eventRaised(MuseEvent event)
        {
        if (event.getType().equals(MuseEventType.StartStep))
            {
            StepEvent start = (StepEvent) event;
            _start_time.put(start.getConfig(), start.getTimestampNanos());
            }
        else if (event.getType().equals(MuseEventType.EndStep))
            {
            StepEvent end = (StepEvent) event;
            Long start_time = _start_time.remove(end.getConfig());
            if (start_time != null)
                {
                final Measurement measurement = new Measurement();
                measurement.setValue(end.getTimestampNanos() - start_time);
                _measurements.add(measurement);
                }
            }
        }

    private HashMap<StepConfiguration, Long> _start_time = new HashMap<>();
    private List<Measurement> _measurements = new ArrayList<>();
    }


