/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.services;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tcs.constants.MigratorConstants;
import com.tcs.database.ConfigurationDetails;
import com.tcs.database.Configurations;
import com.tcs.repositories.ConfigurationRepo;
import com.tcs.repositories.ConfigurationsDetailsRepo;

/**
 * The Class ActiveMQService.
 */
@Service
public class ActiveMQService {
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(ActiveMQService.class);
	
	/** The Constant URL_FORMAT. */
	private static final String URL_FORMAT = "%s://%s:%s";
	
	/** The Constant TEST_MESSAGE. */
	private static final String TEST_MESSAGE = "TestMessage";
	
	private static final String TEST_QUEUENAME = "migratortest";
	
	@Autowired
	ConfigurationRepo configurationRepo;
	
	@Autowired
	ConfigurationsDetailsRepo configurationsDetailsRepo;

	/**
	 * Check connection.
	 *
	 * @param protocol the protocol
	 * @param host the host
	 * @param port the port
	 * @param username the username
	 * @param password the password
	 * @return the JSON object
	 */
	public JSONObject checkConnection(final String protocol,final String host,final int port,final String username, final String password) {
		final JSONObject response = new JSONObject();
		try {
			LOGGER.info("active mq url {}",String.format(URL_FORMAT, protocol,host,port));
		    final ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory();
		    factory.setBrokerURL(String.format(URL_FORMAT, protocol,host,port));
		    final Connection conn = factory.createConnection(username, password);
		    final Session session = ((javax.jms.Connection) conn).createSession(false, Session.AUTO_ACKNOWLEDGE);
		    final MessageProducer producer = session.createProducer(session.createQueue(TEST_QUEUENAME));
		    final MessageConsumer consumer = session.createConsumer(session.createQueue(TEST_QUEUENAME));
		    consumer.setMessageListener(session.getMessageListener()); // class that implements MessageListener
		    conn.start();
		    TextMessage message = new ActiveMQTextMessage();
		    message.setText(TEST_MESSAGE);
		    producer.send(message);
		    producer.close();
		    Message cMessage = consumer.receive(1000);

            if (cMessage instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String text = textMessage.getText();
                LOGGER.info("if Received: " + text);
                response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
                response.put(MigratorConstants.KEY_MESSAGE, "Active MQ connection success");
            } else {
            	LOGGER.info("else Received: " + message);
            	 response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_OK);
                 response.put(MigratorConstants.KEY_MESSAGE, "Active MQ connection success");
            }
            message.clearBody();
            message.acknowledge();
            consumer.close();
            session.close();
            conn.stop();
            conn.close();
		} catch (JMSException jmsException) {
		    LOGGER.error("jmsException occured while executing method checkConnection {}",jmsException);
		    response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            response.put(MigratorConstants.KEY_TRACE, ExceptionUtils.getStackTrace(jmsException) );
            response.put(MigratorConstants.KEY_MESSAGE, jmsException.getMessage());
		} catch (Exception exception) {
			LOGGER.error("exception occured while executing method checkConnection {}",exception);
			response.put(MigratorConstants.KEY_STATUS, HttpStatus.SC_INTERNAL_SERVER_ERROR);
			response.put(MigratorConstants.KEY_TRACE, ExceptionUtils.getStackTrace(exception) );
            response.put(MigratorConstants.KEY_MESSAGE, exception.getMessage());
		}
		return response;
	}
	
	public void saveAcitiveMQDetails(final String protocol,final String host,final String port,final String username, final String password) {
		final Configurations configurations = new Configurations();
		configurations.setConfigurationname(MigratorConstants.ACTIVEMQ);
		
		final ConfigurationDetails configurationDetails = new ConfigurationDetails();
		configurationDetails.setHost(host);
		configurationDetails.setPort(Integer.parseInt(port));
		configurationDetails.setProtocol(protocol);
		configurationDetails.setAppname(MigratorConstants.ACTIVEMQ);
		configurationDetails.setPassword(password);
		configurationDetails.setUsername(username);
		configurationDetails.setConfigurations(configurations);
		configurationsDetailsRepo.save(configurationDetails);
		LOGGER.info("activemq details recorded successfully");
	}
}
