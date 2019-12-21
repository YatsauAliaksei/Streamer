package by.mrj.server.service.sender;

import com.netflix.concurrency.limits.MetricIds;
import com.netflix.concurrency.limits.MetricRegistry;
import com.netflix.concurrency.limits.internal.EmptyMetricRegistry;
import com.netflix.concurrency.limits.limiter.AbstractLimiter;

import java.util.Optional;

public class SendLimiter<ContextT> extends AbstractLimiter<ContextT> {

    public static class Builder extends AbstractLimiter.Builder<Builder> {
        public <ContextT> SendLimiter<ContextT> build() {
            return new SendLimiter<>(this);
        }

        @Override
        protected Builder self() {
            return this;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final MetricRegistry.SampleListener inflightDistribution;

    public SendLimiter(AbstractLimiter.Builder<?> builder) {
        super(builder);

        EmptyMetricRegistry instance = EmptyMetricRegistry.INSTANCE;
        builder.metricRegistry(instance);

        this.inflightDistribution = instance.distribution(MetricIds.INFLIGHT_NAME);
    }

    @Override
    public Optional<Listener> acquire(ContextT context) {
        int currentInFlight = getInflight();
        inflightDistribution.addSample(currentInFlight);
//        if (currentInFlight >= getLimit()) {
//            return createRejectedListener();
//        }
        return Optional.of(createListener());
    }
}
