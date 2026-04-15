package com.oms.fix.kafka;

import com.oms.common.dto.ExecutionReportDTO;
import com.oms.common.kafka.KafkaTopics;
import com.oms.fix.report.ExecutionReportSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecReportConsumer {

    private final ExecutionReportSender sender;

    @KafkaListener(topics = KafkaTopics.EXEC_REPORT, groupId = "fix-gateway")
    public void onExecutionReport(ExecutionReportDTO report) {
        log.info("Received ExecutionReport for ClOrdID={}", report.getClOrdId());
        sender.send(report);
    }
}
