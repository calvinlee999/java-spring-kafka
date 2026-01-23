/*
 * KAFKA MESSAGE ENTITY - DATABASE PERSISTENCE FOR KAFKA EVENTS
 * 
 * Think of this class like a digital filing cabinet record:
 * - Every Kafka message that flows through our system gets filed here
 * - We can search, analyze, and audit all message history
 * - Perfect for event sourcing and message replay scenarios
 * 
 * This is an ENTITY class - it represents a table in our PostgreSQL database.
 * Each instance of this class becomes a row in the 'kafka_messages' table.
 */

package com.learning.kafkagettingstarted.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * ANNOTATIONS EXPLAINED:
 * 
 * @Entity - Tells JPA "This class represents a database table"
 *          It's like putting a label that says "STORE THIS IN DATABASE"
 * 
 * @Table - Specifies the exact table name and any constraints
 *         Like writing the address on an envelope
 * 
 * @Id - Marks the primary key field (unique identifier)
 *      Like a social security number - each record must have a unique one
 * 
 * @GeneratedValue - Tells database to automatically generate IDs
 *                  Like getting a ticket number at the DMV
 * 
 * @Column - Specifies column properties (name, size, constraints)
 *          Like defining the size of each box in a form
 */
@Entity
@Table(name = "kafka_messages", 
       indexes = {
           @Index(name = "idx_topic", columnList = "topic"),
           @Index(name = "idx_created_at", columnList = "created_at"),
           @Index(name = "idx_message_key", columnList = "message_key")
       })
public class KafkaMessage {

    /**
     * PRIMARY KEY - Unique identifier for each message record
     * 
     * Using IDENTITY strategy means PostgreSQL will auto-increment this field.
     * Each new message gets the next available number: 1, 2, 3, 4...
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * KAFKA TOPIC - Which topic this message came from
     * 
     * @NotBlank ensures we always have a topic name
     * @Size limits the length to prevent database overflow
     * Like storing which department handled a document
     */
    @Column(name = "topic", nullable = false, length = 255)
    @NotBlank(message = "Topic cannot be blank")
    @Size(max = 255, message = "Topic must be less than 255 characters")
    private String topic;

    /**
     * MESSAGE KEY - The key part of the Kafka message
     * 
     * Kafka messages have optional keys for partitioning and ordering.
     * We store this for complete message reconstruction.
     */
    @Column(name = "message_key", length = 500)
    @Size(max = 500, message = "Message key must be less than 500 characters")
    private String messageKey;

    /**
     * MESSAGE VALUE - The actual content/payload of the message
     * 
     * This is the main data that was sent through Kafka.
     * Using TEXT type to handle large messages (JSON, XML, etc.)
     */
    @Column(name = "message_value", columnDefinition = "TEXT")
    @NotBlank(message = "Message value cannot be blank")
    private String messageValue;

    /**
     * KAFKA PARTITION - Which partition this message was stored in
     * 
     * Kafka divides topics into partitions for scalability.
     * We track this for debugging and understanding message flow.
     */
    @Column(name = "partition_id")
    private Integer partitionId;

    /**
     * KAFKA OFFSET - The position of this message within its partition
     * 
     * Think of offset like a page number in a book.
     * Each partition has its own numbering: 0, 1, 2, 3...
     */
    @Column(name = "offset_value")
    private Long offsetValue;

    /**
     * MESSAGE STATUS - Processing status of this message
     * 
     * Tracks whether the message was successfully processed, failed, or is pending.
     * Useful for retry logic and monitoring.
     */
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private MessageStatus status = MessageStatus.RECEIVED;

    /**
     * TIMESTAMP FIELDS - Automatic tracking of when records are created/updated
     * 
     * @CreationTimestamp - Hibernate automatically sets this when saving new records
     * @UpdateTimestamp - Hibernate automatically updates this when modifying records
     * 
     * Like having an automatic date stamp on documents
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * PROCESSING DETAILS - Additional information about message processing
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "retry_count", columnDefinition = "integer default 0")
    private Integer retryCount = 0;

    /**
     * DEFAULT CONSTRUCTOR - Required by JPA
     * 
     * JPA needs a no-argument constructor to create instances via reflection.
     * It's like having a blank form that can be filled in later.
     */
    public KafkaMessage() {
    }

    /**
     * CONSTRUCTOR FOR EASY CREATION - Convenience constructor
     * 
     * This makes it easy to create new KafkaMessage instances with the essential data.
     * Like a pre-filled form with the most important fields already completed.
     */
    public KafkaMessage(String topic, String messageKey, String messageValue, Integer partitionId, Long offsetValue) {
        this.topic = topic;
        this.messageKey = messageKey;
        this.messageValue = messageValue;
        this.partitionId = partitionId;
        this.offsetValue = offsetValue;
        this.status = MessageStatus.RECEIVED;
        this.retryCount = 0;
    }

    // GETTERS AND SETTERS - Allow access to private fields
    // These are like controlled access points to the data

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageValue() {
        return messageValue;
    }

    public void setMessageValue(String messageValue) {
        this.messageValue = messageValue;
    }

    public Integer getPartitionId() {
        return partitionId;
    }

    public void setPartitionId(Integer partitionId) {
        this.partitionId = partitionId;
    }

    public Long getOffsetValue() {
        return offsetValue;
    }

    public void setOffsetValue(Long offsetValue) {
        this.offsetValue = offsetValue;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    /**
     * BUSINESS METHODS - Convenient methods for common operations
     */
    public void markAsProcessed() {
        this.status = MessageStatus.PROCESSED;
    }

    public void markAsFailed(String errorMessage) {
        this.status = MessageStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void incrementRetryCount() {
        this.retryCount = (this.retryCount == null) ? 1 : this.retryCount + 1;
    }

    /**
     * toString METHOD - For debugging and logging
     * 
     * Provides a readable representation of the object.
     * Like having a summary card for each message.
     */
    @Override
    public String toString() {
        return "KafkaMessage{" +
                "id=" + id +
                ", topic='" + topic + '\'' +
                ", messageKey='" + messageKey + '\'' +
                ", messageValue='" + messageValue + '\'' +
                ", partitionId=" + partitionId +
                ", offsetValue=" + offsetValue +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", retryCount=" + retryCount +
                '}';
    }

    /**
     * MESSAGE STATUS ENUMERATION - Possible states for a message
     */
    public enum MessageStatus {
        RECEIVED,    // Just arrived from Kafka
        PROCESSING,  // Currently being processed
        PROCESSED,   // Successfully processed
        FAILED,      // Processing failed
        RETRY        // Queued for retry
    }
}
