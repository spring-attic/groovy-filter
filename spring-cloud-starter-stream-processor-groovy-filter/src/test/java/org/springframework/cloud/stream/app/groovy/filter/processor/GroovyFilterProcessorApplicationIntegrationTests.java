/*
 * Copyright 2015-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.stream.app.groovy.filter.processor;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Integration Tests for GroovyFilterProcessor.
 *
 * @author Eric Bottard
 * @author Marius Bogoevici
 * @author Gary Russell
 * @author Artem Bilan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext
public abstract class GroovyFilterProcessorApplicationIntegrationTests {

	@Autowired
	protected Processor channels;

	@Autowired
	protected MessageCollector collector;

	@TestPropertySource(properties = {
			"groovy-filter.script=script.groovy",
			"groovy-filter.variables=threshold=5" })
	public static class UsingScriptIntegrationTests extends GroovyFilterProcessorApplicationIntegrationTests {

		@Test
		public void test() throws InterruptedException {
			channels.input().send(new GenericMessage<Object>("hello"));
			channels.input().send(new GenericMessage<Object>("hello world"));
			channels.input().send(new GenericMessage<Object>("hi!"));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("hello world")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));
		}
	}

	@TestPropertySource(properties = {
			"groovy-filter.script=script-with-grab.groovy"})
	public static class UsingScriptWithGrabIntegrationTests extends GroovyFilterProcessorApplicationIntegrationTests {

		@Test
		public void test() throws InterruptedException {
			channels.input().send(new GenericMessage<Object>(new Float(0.2)));
			channels.input().send(new GenericMessage<Object>(new Float(0.3)));
			channels.input().send(new GenericMessage<Object>(new Float(0.4)));
			assertThat(collector.forChannel(channels.output()), receivesPayloadThat(is("0.2")));
			assertThat(collector.forChannel(channels.output()).poll(10, MILLISECONDS), is(nullValue(Message.class)));
		}
	}

	// Avoid @SpringBootApplication with its @ComponentScan
	@SpringBootConfiguration
	@EnableAutoConfiguration
	@Import(GroovyFilterProcessorConfiguration.class)
	public static class GroovyFilterProcessorApplication {

	}

}
