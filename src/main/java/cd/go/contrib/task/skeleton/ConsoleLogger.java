/*
 * Copyright 2018 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cd.go.contrib.task.skeleton;

import com.google.gson.Gson;
import com.thoughtworks.go.plugin.api.GoApplicationAccessor;
import com.thoughtworks.go.plugin.api.GoPluginIdentifier;
import com.thoughtworks.go.plugin.api.request.DefaultGoApiRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class ConsoleLogger {
    private static final String SEND_CONSOLE_LOG = "go.processor.task.console-log";
    private static ConsoleLogger consoleLogger;
    private final GoApplicationAccessor accessor;

    private ConsoleLogger(GoApplicationAccessor accessor) {
        this.accessor = accessor;
    }

    public void info(String message) {
        sendLog(new ConsoleLogMessage(ConsoleLogMessage.LogLevel.INFO, message));
    }

    public void error(String message) {
        sendLog(new ConsoleLogMessage(ConsoleLogMessage.LogLevel.ERROR, message));
    }

    public void process(InputStream out, InputStream error) throws IOException {
        processInputStream(out);
        processErrorStream(error);
    }

    public void processErrorStream(InputStream error) throws IOException {
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(error));
        String errorLine;
        while ((errorLine = errorReader.readLine()) != null) {
            consoleLogger.error(errorLine);
        }
    }

    public void processInputStream(InputStream out) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(out));
        String infoLine;
        while ((infoLine = in.readLine()) != null) {
            consoleLogger.info(infoLine);
        }
    }

    private void sendLog(ConsoleLogMessage consoleLogMessage) {
        DefaultGoApiRequest request = new DefaultGoApiRequest(SEND_CONSOLE_LOG, "1.0", new GoPluginIdentifier("task", Arrays.asList("1.0")));
        request.setRequestBody(consoleLogMessage.toJSON());
        accessor.submit(request);
    }

    public static ConsoleLogger getLogger(GoApplicationAccessor accessor) {
        if (consoleLogger == null) {
            synchronized (ConsoleLogger.class) {
                if (consoleLogger == null) {
                    consoleLogger = new ConsoleLogger(accessor);
                }
            }
        }

        return consoleLogger;
    }

    static class ConsoleLogMessage {
        private LogLevel logLevel;
        private String message;

        public ConsoleLogMessage(LogLevel logLevel, String message) {
            this.message = message;
            this.logLevel = logLevel;
        }

        public String toJSON() {
            return new Gson().toJson(this);
        }

        enum LogLevel {
            INFO, ERROR
        }
    }
}
