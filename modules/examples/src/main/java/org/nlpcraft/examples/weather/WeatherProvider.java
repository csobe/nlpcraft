/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *     _   ____      ______           ______
 *    / | / / /___  / ____/________ _/ __/ /_
 *   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
 *  / /|  / / /_/ / /___/ /  / /_/ / __/ /_
 * /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
 *        /_/
 */

package org.nlpcraft.examples.weather;

import org.nlpcraft.examples.weather.apixu.*;
import org.nlpcraft.examples.weather.apixu.beans.*;
import org.nlpcraft.mdllib.*;
import org.nlpcraft.mdllib.intent.*;
import org.nlpcraft.mdllib.intent.NCIntentSolver.*;
import org.nlpcraft.mdllib.tools.builder.*;
import org.nlpcraft.mdllib.utils.*;
import org.apache.commons.lang3.tuple.*;
import java.text.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static org.nlpcraft.mdllib.utils.NCTokenUtils.*;

/**
 * Weather example model provider.
 * <p>
 * This is relatively complete weather service with elaborate HTML output and a non-trivial
 * intent matching logic. It uses https://www.apixu.com REST service for the actual
 * weather information.
 */
@NCActiveModelProvider
public class WeatherProvider extends NCModelProviderAdapter {
    // It is demo token and its usage has some restrictions (history data contains one day only, etc).
    // Please register your own account at https://www.apixu.com/pricing.aspx and
    // replace this demo token with your own.
    private ApixuWeatherService srv = new ApixuWeatherService("3f9b7de2d3894eb6b27150825171909");

    // Date formats.
    private final static DateFormat inFmt = new SimpleDateFormat("yyyy-MM-dd");
    // We'll use string substitution here for '___' piece...
    private final static DateFormat outFmt = new SimpleDateFormat("EE'<br/><span style=___>'MMM dd'</span>'");
    // Base CSS.
    private static final String CSS = "style='display: inline-block; min-width: 120px'";
    // Maximum free words left before auto-curation.
    private static final int MAX_FREE_WORDS = 4;
    // Keywords for 'local' weather.
    private static final Set<String> LOCAL_WORDS = new HashSet<>(Arrays.asList("my", "local", "hometown"));
    
    /**
     * A shortcut to convert millis to the local date object.
     *
     * @param ms Millis to convert.
     * @return Local date object.
     */
    private LocalDate toLocalDate(long ms) {
        return Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Makes Google static map fragment for given coordinates.
     *
     * @param lat Latitude.
     * @param lon Longitude.
     * @return Query result fragment.
     */
    private NCQueryResult makeMap(String lat, String lon) {
        double dLat = Double.parseDouble(lat);
        double dLon = Double.parseDouble(lon);

        return NCQueryResult.jsonGmap(
            String.format(
                "{" +
                    "\"cssStyle\": {" +
                        "\"width\": \"600px\", " +
                        "\"height\": \"300px\"" +
                    "}," +
                    "\"gmap\": {" +
                        "\"center\": \"%f,%f\"," +
                        "\"zoom\": 4," +
                        "\"scale\": 2," +
                        "\"size\": \"600x300\", " +
                        "\"maptype\": \"terrain\", " +
                        "\"markers\": \"color:red|%f,%f\"" +
                    "}" +
                "}",
                dLat,
                dLon,
                dLat,
                dLon
            )
        );
    }

    /**
     * Utility to prepare HTML table header with given date.
     *
     * @param date Date for the header.
     * @return HTML fragment.
     */
    private String prepHeader(String date) {
        try {
            return
                "\"<center style='color: #add'>" +
                outFmt.format(inFmt.parse(date)).replace("___", "'font-weight: 200; font-size: 80%'") +
                "</center>\"";
        }
        catch (ParseException e) {
            return date;
        }
    }

    /**
     * Utility to prepare HTML table cell with given day data.
     *
     * @param day Day's weather holder.
     * @return HTML fragment.
     */
    private String prepCell(Day day) {
        String css1 = "font-size: 90%; color: #fff; font-weight: 200; letter-spacing: 0.05em";
        String css2 = "font-size: 100%; color: #fff; font-weight: 400; letter-spacing: 0.05em";

        return String.format("\"" +
            "<center>" +
                "<img style='margin-bottom: 5px;' title='%s' src='%s'><br/>" +
                "<span style='%s'>%s F <span style='font-size: 80%%'> - %s F</span></span><br/>" +
                "<span style='%s'>Humidity %d%%</span><br/>" +
                "<span style='%s'>Rain %s in</span><br/>" +
                "<span style='%s'>Wind %s mph</span>" +
            "</center>\"",
            day.getCondition().getText(), day.getCondition().getIcon(),
            css2, Math.round(day.getMaxTempF()), Math.round(day.getMinTempF()),
            css1, day.getAvgHumidity(),
            css1, Math.round(day.getTotalPrecipIn()),
            css1, Math.round(day.getMaxWindMph())
        );
    }

    /**
     * Makes query multipart result for given date range weather.
     *
     * @param res Weather holder for the range of dates.
     * @return Query result.
     */
    private NCQueryResult makeRangeResult(RangeResponse res) {
        Location loc = res.getLocation();

        if (loc == null)
            throw new NCRejection("Weather service doesn't recognize this location.");

        String headers = Arrays.stream(res.getForecast().getForecastDay()).map(day ->
            prepHeader(day.getDate())).collect(Collectors.joining(","));
        String rows = Arrays.stream(res.getForecast().getForecastDay()).map(day ->
            prepCell(day.getDay())).collect(Collectors.joining(","));

        // Multipart result (HTML block + HTML table + Google map).
        return NCQueryResult.jsonMultipart(
            // 1. HTML fragment.
            NCQueryResult.html(
                String.format(
                    "<b %s>City:</b> <span style='color: #F1C40F'>%s</span><br/>" +
                    "<b %s>Local Time:</b> %s",
                    CSS, loc.getName() + ", " + loc.getRegion(),
                    CSS, loc.getLocaltime()
                )
            ),
            // 2. HTML table fragment.
            NCQueryResult.jsonTable(
                String.format(
                    "{" +
                        "\"border\": true," +
                        "\"background\": \"#2f4963\"," +
                        "\"borderColor\": \"#607d8b\"," +
                        "\"columns\": [%s]," +
                        "\"rows\": [[%s]]" +
                    "}",
                    headers,
                    rows
                )
            ),
            // 3. Static Google map.
            makeMap(loc.getLatitude(), loc.getLongitude())
        );
    }

    /**
     * Makes query multipart result for single date.
     *
     * @param res Weather holder for single date.
     * @return Query result.
     */
    private NCQueryResult makeCurrentResult(CurrentResponse res) {
        Location loc = res.getLocation();
        Current cur = res.getCurrent();

        if (loc == null)
            throw new NCRejection("Weather service doesn't recognize this location.");
        if (cur == null)
            throw new NCRejection("Weather service doesn't support this location.");

        // Multipart result (HTML block + Google map).
        return NCQueryResult.jsonMultipart(
            // 1. HTML block fragment.
            NCQueryResult.html(
                String.format(
                    "<div style='font-weight: 200; letter-spacing: 0.02em'>" +
                        "<b %s>Conditions:</b> <b><span style='color: #F1C40F'>%s, %s F</span></b><br/>" +
                        "<b %s>City:</b> %s<br/>" +
                        "<b %s>Humidity:</b> %d%%<br/>" +
                        "<b %s>Rain:</b> %s in<br/>" +
                        "<b %s>Wind:</b> %s %s mph<br/>" +
                        "<b %s>Local Time:</b> %s<br/>" +
                    "</div>",
                    CSS, cur.getCondition().getText(), Math.round(cur.getTempF()),
                    CSS, loc.getName() + ", " + loc.getRegion(),
                    CSS, cur.getHumidity(),
                    CSS, Math.round(cur.getPrecipIn()),
                    CSS, cur.getWindDir(), cur.getWindMph(),
                    CSS, loc.getLocaltime()
                )
            ),
            // 2. Google map fragment.
            makeMap(loc.getLatitude(), loc.getLongitude())
        );
    }

    /**
     * Extracts date range from given solver context.
     *
     * @param ctx Solver context.
     * @return Pair of dates or {@code null} if not found.
     */
    private Pair<LocalDate, LocalDate> prepDate(NCIntentSolverContext ctx) {
        List<NCToken> toks = ctx.getIntentTokens().get(1);

        if (toks.size() > 1)
            throw new NCRejection("Only one date is supported.");

        if (toks.size() == 1) {
            NCToken tok = toks.get(0);

            return Pair.of(toLocalDate(getDateFrom(tok)), toLocalDate(getDateTo(tok)));
        }

        // No date found - return 'null'.
        return null;
    }

    /**
     * Extracts geo location (city) from given solver context that is suitable for APIXU service.
     *
     * @param ctx Solver context.
     * @return Geo location.
     */
    private String prepGeo(NCIntentSolverContext ctx) throws NCRejection {
        List<NCToken> geoToks = ctx.getIntentTokens().get(2); // Can be empty...
        NCSentence sen = ctx.getQueryContext().getSentence();
        List<NCToken> allToks = ctx.getVariant().getTokens();

        // Common lambda for getting current user city.
        Supplier<String> curCityFn = () -> {
            // Try current user location.
            if (sen.getLatitude().isPresent() && sen.getLongitude().isPresent())
                // APIXU weather service understands this format too.
                return sen.getLatitude().get() + "," + sen.getLongitude().get();
            else
                throw new NCRejection("City cannot be determined.");
        };

        // Manually process request for local weather. We need to separate between 'local Moscow weather'
        // and 'local weather' which are different. Basically, if there is word 'local/my/hometown' in the user
        // input and there is no city in the current sentence - this is a request for the weather at user's
        // current location, i.e. we should implicitly assume user's location and clear conversion context.
        // In all other cases - we take location from either current sentence or conversation STM.

        // NOTE: we don't do this separation on intent level as it is easier to do it here instead of
        // creating more intents with almost identical callbacks.

        boolean hasLocalWord = allToks.stream().anyMatch(t -> LOCAL_WORDS.contains(getNormalizedText(t)));

        if (hasLocalWord && geoToks.isEmpty()) {
            // Because we implicitly assume user's current city at this point we need to clear
            // 'nlp:geo' tokens from conversation context since they would no longer be valid.
            ctx.getQueryContext().getConversationContext().clear(NCTokenUtils::isGeo);

            // Return user current city.
            return curCityFn.get();
        }
        else
            return geoToks.size() == 1 ? getGeoCity(geoToks.get(0)) : curCityFn.get();
    }

    /**
     * Makes query result for given date range.
     *
     * @param ctx Token solver context.
     * @param from Default from date.
     * @param to Default to date.
     * @return Query result.
     */
    private NCQueryResult onRangeMatch(NCIntentSolverContext ctx, LocalDate from, LocalDate to) {
        Pair<LocalDate, LocalDate> date = prepDate(ctx);

        if (date == null)
            // If we don't have the date in the sentence or conversation STM - use provided range.
            date = Pair.of(from, to);

        String geo = prepGeo(ctx);

        try {
            return makeRangeResult(srv.getWeather(geo, date));
        }
        catch (ApixuPeriodException e) {
            throw new NCRejection(e.getMessage());
        }
    }

    /**
     * Strict check for an exact match (i.e. no dangling unused system or user defined tokens) and
     * maximum number of free words left unmatched. In both cases user input will go into curation.
     *
     * @param ctx Solver context.
     */
    private void checkMatch(NCIntentSolverContext ctx) {
        // Send for curation if intent match is not exact ("dangling" tokens remain).
        if (!ctx.isExactMatch())
            throw new NCCuration("Intent match was not exact.");
        
        // Send for curation if there are too many free words left unmatched.
        if (ctx.getVariant().stream(NCTokenUtils::isFreeWord).count() > MAX_FREE_WORDS)
            throw new NCCuration("Too many free words.");
    }

    /**
     * Callback on forecast intent match.
     *
     * @param ctx Token solver context.
     * @return Query result.
     */
    private NCQueryResult onForecastMatch(NCIntentSolverContext ctx) {
        checkMatch(ctx);
        
        try {
            // Look 5 days ahead by default.
            return onRangeMatch(ctx, LocalDate.now(), LocalDate.now().plusDays(5));
        }
        catch (NCRejection | NCCuration e) {
            throw e;
        }
        catch (Exception e) {
            throw new NCRejection("Weather provider error.", e);
        }
    }

    /**
     * Callback on history intent match.
     *
     * @param ctx Token solver context.
     * @return Query result.
     */
    private NCQueryResult onHistoryMatch(NCIntentSolverContext ctx) {
        checkMatch(ctx);

        try {
            // Look 5 days back by default.
            return onRangeMatch(ctx, LocalDate.now().minusDays(5), LocalDate.now());
        }
        catch (NCRejection | NCCuration e) {
            throw e;
        }
        catch (Exception e) {
            throw new NCRejection("Weather provider error.", e);
        }
    }

    /**
     * Callback on current date intent match.
     *
     * @param ctx Token solver context.
     * @return Query result.
     */
    private NCQueryResult onCurrentMatch(NCIntentSolverContext ctx) {
        checkMatch(ctx);

        try {
            Pair<LocalDate, LocalDate> date = prepDate(ctx);
            String geo = prepGeo(ctx);
    
            return date != null ?
                makeRangeResult(srv.getWeather(geo, date)) :
                makeCurrentResult(srv.getCurrentWeather(geo));
        }
        catch (ApixuPeriodException e) {
            throw new NCRejection(e.getMessage());
        }
        catch (NCRejection | NCCuration e) {
            throw e;
        }
        catch (Exception e) {
            throw new NCRejection("Weather provider error.", e);
        }
    }

    /**
     * Shortcut for creating a conversational intent with given weather token.
     *
     * @param weatherTokId Specific weather token ID.
     * @return Newly created intent.
     */
    private INTENT mkIntent(String id, String weatherTokId) {
        return new CONV_INTENT(
            id,
            new TERM("id == " + weatherTokId, 1, 1),     // Index 0: mandatory 'weather' token.
            new TERM("id == nlp:date", 0, 1),           // Index 1: optional date.
            new TERM(                                  // Index 2: optional city.
                new AND("id == nlp:geo", "~GEO_KIND == CITY"),
                0, 1
            )
        );
    }

    /**
     * Initializes model provider.
     */
    WeatherProvider() {
        String modelPath = NCModelBuilder.classPathFile("weather_model.json");

        // If no intent is matched respond with some helpful message...
        NCIntentSolver solver = new NCIntentSolver("solver", () -> {
            throw new NCRejection(
                "Weather request is ambiguous.<br>" +
                "Note: only one optional <b>city</b> and one optional <b>date</b> or <b>date range</b> are allowed.");
        });

        // Match exactly one of weather tokens and optional 'nlp:geo' and 'nlp:date' tokens.
        solver.addIntent(mkIntent("hist|date?|city?", "wt:hist"), this::onHistoryMatch);
        solver.addIntent(mkIntent("fcast|date?|city?", "wt:fcast"), this::onForecastMatch);
        solver.addIntent(mkIntent("curr|date?|city?", "wt:curr"), this::onCurrentMatch);

        setup(NCModelBuilder
            .newJsonModel(modelPath)
            .setQueryFunction(solver::solve)
            .build()
        );
    }
}