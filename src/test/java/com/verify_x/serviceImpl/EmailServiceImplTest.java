package com.verify_x.serviceImpl;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {

        emailService = new EmailServiceImpl(mailSender);

        ReflectionTestUtils.setField(
                emailService,
                "fromEmail",
                "verifyx@test.com");
    }

    @Test
    void testSendApplicationApprovedEmailSuccess() {

        MimeMessage mimeMessage =
                new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        doNothing().when(mailSender).send(any(MimeMessage.class));

        emailService.sendApplicationApprovedEmail(
                "john@test.com",
                "John",
                "Congratulations");

        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendApplicationRejectedEmailSuccess() {

        MimeMessage mimeMessage =
                new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        emailService.sendApplicationRejectedEmail(
                "john@test.com",
                "John",
                "Rejected");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendReUploadRequestEmailSuccess() {

        MimeMessage mimeMessage =
                new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage())
                .thenReturn(mimeMessage);

        emailService.sendReUploadRequestEmail(
                "john@test.com",
                "John",
                "Upload PAN");

        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void testSendApplicationApprovedMailException() {

        when(mailSender.createMimeMessage())
                .thenThrow(new MailSendException("Mail Error"));

        assertThrows(
                RuntimeException.class,
                () -> emailService.sendApplicationApprovedEmail(
                        "john@test.com",
                        "John",
                        "Approved"));

        verify(mailSender).createMimeMessage();
    }

    @Test
    void testSendApplicationRejectedMailException() {

        when(mailSender.createMimeMessage())
                .thenThrow(new MailSendException("Mail Error"));

        assertThrows(
                RuntimeException.class,
                () -> emailService.sendApplicationRejectedEmail(
                        "john@test.com",
                        "John",
                        "Rejected"));
    }

    @Test
    void testSendReUploadMailException() {

        when(mailSender.createMimeMessage())
                .thenThrow(new MailSendException("Mail Error"));

        assertThrows(
                RuntimeException.class,
                () -> emailService.sendReUploadRequestEmail(
                        "john@test.com",
                        "John",
                        "Upload PAN"));
    }
}