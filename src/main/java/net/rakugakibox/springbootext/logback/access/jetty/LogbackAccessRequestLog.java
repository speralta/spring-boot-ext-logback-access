package net.rakugakibox.springbootext.logback.access.jetty;

import ch.qos.logback.access.jetty.RequestLogImpl;
import ch.qos.logback.access.spi.AccessContext;
import ch.qos.logback.core.spi.FilterReply;
import lombok.Getter;
import lombok.Setter;
import net.rakugakibox.springbootext.logback.access.LogbackAccessConfigurator;
import net.rakugakibox.springbootext.logback.access.LogbackAccessProperties;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

/**
 * The Jetty request-log.
 * This class is own implementation from scratch, based on {@link RequestLogImpl} and {@link NCSARequestLog}.
 */
public class LogbackAccessRequestLog extends AbstractLifeCycle implements RequestLog {

    /**
     * The Logback-access context.
     */
    @Getter
    private final AccessContext context;

    /**
     * The configuration properties.
     */
    @Getter
    @Setter
    private LogbackAccessProperties properties;

    /**
     * The configurator.
     */
    @Getter
    @Setter
    private LogbackAccessConfigurator configurator;

    /**
     * Constructs an instance.
     */
    public LogbackAccessRequestLog() {

        // Creates the Logback-access context.
        context = new AccessContext();
        context.setName(toString());

    }

    /** {@inheritDoc} */
    @Override
    protected void doStart() throws Exception {

        // Configures and starts the Logback-access context.
        configurator.configure(context);
        context.start();

        super.doStart();
    }

    /** {@inheritDoc} */
    @Override
    protected void doStop() throws Exception {
        super.doStop();

        // Stops and resets the Logback-access context.
        context.stop();
        context.reset();
        context.detachAndStopAllAppenders();
        context.clearAllFilters();

    }

    /** {@inheritDoc} */
    @Override
    public void log(Request request, Response response) {

        // Creates a access event.
        JettyAccessEvent accessEvent = new JettyAccessEvent(request, response);
        accessEvent.setThreadName(Thread.currentThread().getName());
        accessEvent.setUseServerPortInsteadOfLocalPort(properties.getUseServerPortInsteadOfLocalPort());

        // Calls appenders.
        if (context.getFilterChainDecision(accessEvent) != FilterReply.DENY) {
            context.callAppenders(accessEvent);
        }

    }

}
