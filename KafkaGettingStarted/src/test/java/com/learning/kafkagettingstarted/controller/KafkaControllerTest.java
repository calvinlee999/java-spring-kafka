package com.learning.kafkagettingstarted.controller;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.kafkagettingstarted.entity.KafkaMessage;
import com.learning.kafkagettingstarted.service.KafkaMessagePersistenceService;
import com.learning.kafkagettingstarted.service.KafkaProducerService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.learning.kafkagettingstarted.entity.KafkaMessage;
import com.learning.kafkagettingstarted.service.KafkaMessagePersistenceService;
import com.learning.kafkagettingstarted.service.KafkaProducerService;

@WebMvcTest(KafkaController.class)
class KafkaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @MockBean
    private KafkaMessagePersistenceService persistenceService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(kafkaProducerService, persistenceService);
    }

    @Test
    void testSendOrderMessage_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("key", "test-key");
        request.put("message", "Test order message");

        // Mock the service call
        doNothing().when(kafkaProducerService).sendOrderMessage(anyString(), anyString());

        mockMvc.perform(post("/api/kafka/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Message sent"))
                .andExpect(jsonPath("$.key").value("test-key"))
                .andExpect(jsonPath("$.message").value("Test order message"))
                .andExpect(jsonPath("$.topic").value("kafka.learning.orders"));

        verify(kafkaProducerService, times(1)).sendOrderMessage("test-key", "Test order message");
    }

    @Test
    void testSendUseCaseMessage_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("key", "usecase-key");
        request.put("message", "Test usecase message");

        // Mock the service call
        doNothing().when(kafkaProducerService).sendUseCaseMessage(anyString(), anyString());

        mockMvc.perform(post("/api/kafka/usecase")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Message sent"))
                .andExpect(jsonPath("$.key").value("usecase-key"))
                .andExpect(jsonPath("$.message").value("Test usecase message"))
                .andExpect(jsonPath("$.topic").value("kafka.learning.usecase"));

        verify(kafkaProducerService, times(1)).sendUseCaseMessage("usecase-key", "Test usecase message");
    }

    @Test
    void testGetAllMessages() throws Exception {
        // Create mock KafkaMessage entities
        KafkaMessage message1 = new KafkaMessage();
        message1.setId(1L);
        message1.setMessageKey("key1");
        message1.setMessageValue("Message 1");
        message1.setTopic("kafka.learning.orders");
        
        KafkaMessage message2 = new KafkaMessage();
        message2.setId(2L);
        message2.setMessageKey("key2");
        message2.setMessageValue("Message 2");
        message2.setTopic("kafka.learning.usecase");

        List<KafkaMessage> messages = Arrays.asList(message1, message2);
        Page<KafkaMessage> messagePage = new PageImpl<>(messages, PageRequest.of(0, 100), messages.size());

        when(persistenceService.getAllMessages(0, 100)).thenReturn(messagePage);

        mockMvc.perform(get("/api/kafka/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].messageValue").value("Message 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].messageValue").value("Message 2"));

        verify(persistenceService, times(1)).getAllMessages(0, 100);
    }

    @Test
    void testGetMessageStats() throws Exception {
        Map<String, Long> topicCounts = new HashMap<>();
        topicCounts.put("kafka.learning.orders", 25L);
        topicCounts.put("kafka.learning.usecase", 17L);
        
        when(persistenceService.getTotalMessageCount()).thenReturn(42L);
        when(persistenceService.getMessageCountByTopic()).thenReturn(topicCounts);
        when(persistenceService.getLatestMessageTime()).thenReturn(LocalDateTime.now());

        mockMvc.perform(get("/api/kafka/messages/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalMessages").value(42));

        verify(persistenceService, times(1)).getTotalMessageCount();
        verify(persistenceService, times(1)).getMessageCountByTopic();
        verify(persistenceService, times(1)).getLatestMessageTime();
    }
}
