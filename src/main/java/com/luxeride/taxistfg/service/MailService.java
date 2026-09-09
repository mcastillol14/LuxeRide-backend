package com.luxeride.taxistfg.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void enviarMail(String email, String nombre) throws MessagingException {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

        helper.setTo(email);
        helper.setSubject("¡Bienvenido a LuxeRide!");
        helper.setFrom("***");


        String contenidoHtml = """
                <html>
                <head>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            margin: 0;
                            padding: 0;
                            background-color: #f4f4f4;
                        }
                        .container {
                            max-width: 600px;
                            margin: 20px auto;
                            background: #ffffff;
                            padding: 20px;
                            border-radius: 8px;
                            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
                        }
                        .header {
                            background-color: #2E7D32;
                            color: white;
                            text-align: center;
                            padding: 15px;
                            font-size: 24px;
                            border-top-left-radius: 8px;
                            border-top-right-radius: 8px;
                        }
                        .content {
                            padding: 20px;
                            text-align: center;
                        }
                        .button {
                            display: inline-block;
                            background-color: #2E7D32;
                            color: white;
                            padding: 12px 24px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin-top: 20px;
                            font-size: 16px;
                        }
                        a {
                            color: white !important;
                             text-decoration: none;
                        }
                        .footer {
                            text-align: center;
                            font-size: 12px;
                            color: #777;
                            margin-top: 20px;
                        }
                        @media screen and (max-width: 600px) {
                            .container {
                                width: 100%;
                                padding: 10px;
                            }
                            .content {
                                padding: 10px;
                            }
                            .button {
                                width: 80%;
                                padding: 10px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            ¡Bienvenido a LuxeRide!
                        </div>
                        <div class="content">
                            <p>Gracias por registrarte en <b>LuxeRide</b>. Nos emociona tenerte a bordo.</p>
                            <p>Con LuxeRide, disfrutarás de un servicio de transporte de lujo con la mejor comodidad.</p>
                            <a href="https://luxeride.mcastillol14.online" class="button">Explorar LuxeRide</a>
                        </div>
                        <div class="footer">
                            &copy; 2025 LuxeRide. Todos los derechos reservados.
                        </div>
                    </div>
                </body>
                </html>
                """;

        helper.setText(contenidoHtml, true);
        mailSender.send(mensaje);
    }
}
