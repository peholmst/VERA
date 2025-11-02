/*
 * Copyright (c) 2025 Petter Holmström
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

package net.pkhapps.vera.gis.server.data;

import net.pkhapps.vera.gis.server.data.postgis.PostGIS;
import net.pkhapps.vera.gis.server.data.postgis.PostGISSettings;
import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

@NullMarked
abstract class AbstractImportDataApplication {

    static final Option DB_HOST = Option.builder()
            .longOpt("db-host")
            .argName("hostname")
            .hasArg()
            .desc("Database hostname")
            .get();
    static final Option DB_PORT = Option.builder()
            .longOpt("db-port")
            .argName("port")
            .hasArg()
            .desc("Database port")
            .get();
    static final Option DB_NAME = Option.builder()
            .longOpt("db-name")
            .argName("database")
            .hasArg()
            .desc("Database name")
            .get();
    static final Option DB_USER = Option.builder()
            .longOpt("db-user")
            .argName("username")
            .hasArg()
            .desc("Database username")
            .get();
    static final Option DB_PASSWORD_FILE = Option.builder()
            .longOpt("db-password-file")
            .argName("filename")
            .hasArg()
            .desc("Path to a file that contains the database password")
            .get();

    protected Options createOptions() {
        var options = new Options();
        options.addOption(DB_HOST);
        options.addOption(DB_PORT);
        options.addOption(DB_NAME);
        options.addOption(DB_USER);
        options.addOption(DB_PASSWORD_FILE);
        return options;
    }

    protected abstract String getCommandLineSyntax();

    final void run(String[] args) throws ParseException, IOException, SQLException {
        var options = createOptions();
        var parser = new DefaultParser();
        var commandLine = parser.parse(options, args);
        var arguments = commandLine.getArgList();
        if (!validateArguments(arguments)) {
            var helpFormatter = HelpFormatter.builder().setShowSince(false).get();
            helpFormatter.printHelp(getCommandLineSyntax(), "", options, "", false);
            return;
        }
        var settings = getSettings(commandLine);
        try (var postgis = new PostGIS(settings)) {
            importData(arguments, postgis);
        }
    }

    protected boolean validateArguments(List<String> arguments) {
        return !arguments.isEmpty();
    }

    protected abstract void importData(List<String> arguments, PostGIS postgis) throws IOException, SQLException;

    private PostGISSettings getSettings(CommandLine commandLine) throws ParseException, IOException {
        var passwordFile = Path.of(commandLine.getOptionValue(DB_PASSWORD_FILE, "vera-gis-password")).toAbsolutePath();
        var password = Files.readString(passwordFile).trim();
        return new PostGISSettings(
                commandLine.getOptionValue(DB_HOST, "localhost"),
                commandLine.getParsedOptionValue(DB_PORT, 5432),
                commandLine.getOptionValue(DB_NAME, "vera_gis"),
                commandLine.getOptionValue(DB_USER, "vera_gis"),
                password,
                true
        );
    }
}
