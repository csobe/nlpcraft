/*
 * “Commons Clause” License, https://commonsclause.com/
 *
 * The Software is provided to you by the Licensor under the License,
 * as defined below, subject to the following condition.
 *
 * Without limiting other conditions in the License, the grant of rights
 * under the License will not include, and the License does not grant to
 * you, the right to Sell the Software.
 *
 * For purposes of the foregoing, “Sell” means practicing any or all of
 * the rights granted to you under the License to provide to third parties,
 * for a fee or other consideration (including without limitation fees for
 * hosting or consulting/support services related to the Software), a
 * product or service whose value derives, entirely or substantially, from
 * the functionality of the Software. Any license notice or attribution
 * required by the License must also include this Commons Clause License
 * Condition notice.
 *
 * Software:    NlpCraft
 * License:     Apache 2.0, https://www.apache.org/licenses/LICENSE-2.0
 * Licensor:    Copyright (C) 2018 DataLingvo, Inc. https://www.datalingvo.com
 *
 *     _   ____      ______           ______
 *    / | / / /___  / ____/________ _/ __/ /_
 *   /  |/ / / __ \/ /   / ___/ __ `/ /_/ __/
 *  / /|  / / /_/ / /___/ /  / /_/ / __/ /_
 * /_/ |_/_/ .___/\____/_/   \__,_/_/  \__/
 *        /_/
 */

package org.nlpcraft.mdllib.tools.dev;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.nlpcraft.mdllib.NCQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test client builder for {@link NCTestClient} instances.
 */
public class NCTestClientBuilder {
    /** Default public REST API URL (endpoint). */
    public static final String DFLT_BASEURL = "http://localhost:8081/api/v1/";
    
    /** Default client email. */
    public static final String DFLT_EMAIL = "admin@admin.com";
    
    /** Default client password. */
    public static final String DFLT_PASSWORD = "admin";
    
    /** Default maximum statuses check time, millisecond. */
    public static final long DFLT_MAX_CHECK_TIME = 10 * 1000;
    
    /** Default millisecond delay between result checks. */
    public static final long DFLT_CHECK_INTERVAL_MS = 2000;
    
    /** Default clear conversation flag value. */
    public static final boolean DFLT_CLEAR_CONVERSATION = false;
    
    /** Default asynchronous mode flag value. */
    public static final boolean DFLT_ASYNC_MODE = true;
    
    private static final Logger log = LoggerFactory.getLogger(NCTestClientBuilder.class);
    
    private long checkIntervalMs = DFLT_CHECK_INTERVAL_MS;
    private boolean clearConv = DFLT_CLEAR_CONVERSATION;
    private boolean asyncMode = DFLT_ASYNC_MODE;
    private long maxCheckTime = DFLT_MAX_CHECK_TIME;
    private RequestConfig reqCfg;
    private String baseUrl = DFLT_BASEURL;
    private String email = DFLT_EMAIL;
    private String pswd = DFLT_PASSWORD;
    private Supplier<CloseableHttpClient> cliSup;
    
    /**
     * JSON helper class.
     */
    static class NCDsJson {
        @SerializedName("id") private long dsId;
        @SerializedName("mdlId") private String mdlId;
    
        public long getDatasourceId() {
            return dsId;
        }
    
        public void setDatasourceId(long dsId) {
            this.dsId = dsId;
        }
    
        public String getModelId() {
            return mdlId;
        }
    
        public void setModelId(String mdlId) {
            this.mdlId = mdlId;
        }
    }
        
        
    /**
     * JSON helper class.
     */
    static class NCRequestStateJson {
        @SerializedName("srvReqId") private String srvReqId;
        @SerializedName("usrId") private long userId;
        @SerializedName("dsId") private long dsId;
        @SerializedName("resType") private String resType;
        @SerializedName("resBody") private String resBody;
        @SerializedName("status") private String status;
        @SerializedName("error") private String error;
        @SerializedName("createTstamp") private long createTstamp;
        @SerializedName("updateTstamp") private long updateTstamp;
    
        public String getServerRequestId() {
            return srvReqId;
        }
    
        public void setServerRequestId(String srvReqId) {
            this.srvReqId = srvReqId;
        }
    
        public long getUserId() {
            return userId;
        }
    
        public void setUserId(long userId) {
            this.userId = userId;
        }
    
        public long getDatasourceId() {
            return dsId;
        }
    
        public void setDatasourceId(long dsId) {
            this.dsId = dsId;
        }
    
        public String getStatus() {
            return status;
        }
    
        public void setStatus(String status) {
            this.status = status;
        }
    
        public String getResultType() {
            return resType;
        }
    
        public void setResultType(String resType) {
            this.resType = resType;
        }
    
        public String getResultBody() {
            return resBody;
        }
    
        public void setResultBody(String resBody) {
            this.resBody = resBody;
        }
    
        public String getError() {
            return error;
        }
    
        public void setError(String error) {
            this.error = error;
        }
    
        public long getCreateTstamp() {
            return createTstamp;
        }
    
        public void setCreateTstamp(long createTstamp) {
            this.createTstamp = createTstamp;
        }
    
        public long getUpdateTstamp() {
            return updateTstamp;
        }
    
        public void setUpdateTstamp(long updateTstamp) {
            this.updateTstamp = updateTstamp;
        }
    }
    
    private static class AsciiTable {
        private final List<Pair<String, Integer>> cols;
        private final List<List<String>> rows;
        
        AsciiTable(String... cols) {
            this.cols = Arrays.stream(cols).map(p -> MutablePair.of(p, 0)).collect(Collectors.toList());
            this.rows = new ArrayList<>();
        }
        
        private void writeColumnNames(List<Pair<String, Integer>> cols, StringBuilder buf) {
            buf.append('|');
            
            for (Pair<String, Integer> col : cols) {
                buf.append(String.format(" %-" + col.getValue() + 's', col.getKey()));
                
                buf.append('|');
            }
            
            buf.append('\n');
        }
        
        private void writeSeparator(List<Pair<String, Integer>> cols, StringBuilder buf) {
            buf.append('+');
            
            for (Pair<String, Integer> col : cols) {
                buf.append(String.format("%-" + (col.getValue() + 1) + 's', "").replace(' ', '-'));
                
                buf.append('+');
            }
            
            buf.append('\n');
        }
        
        private void writeValues(List<Pair<String, Integer>> cols, List<List<String>> rows, StringBuilder buf) {
            for (List<String> row : rows) {
                int idx = 0;
                
                buf.append('|');
                
                for (String cell : row) {
                    buf.append(String.format(" %-" + cols.get(idx).getValue() + 's', cell));
                    
                    buf.append('|');
                    
                    idx++;
                }
                
                buf.append('\n');
            }
        }
        
        void addRow(List<Object> row) {
            rows.add(row.stream().map(p -> p != null ? p.toString() : "").collect(Collectors.toList()));
        }
    
        String mkContent() {
            cols.forEach(col -> col.setValue(col.getKey().length() + 1));
        
            for (List<String> row : rows) {
                int i = 0;
            
                for (String cell : row) {
                    if (cell != null) {
                        Pair<String, Integer> col = cols.get(i);
                    
                        col.setValue(Math.max(col.getValue(), cell.length() + 1));
                    
                        i++;
                    }
                }
            }
        
            StringBuilder buf = new StringBuilder();
        
            writeSeparator(cols, buf);
            writeColumnNames(cols, buf);
            writeSeparator(cols, buf);
            writeValues(cols, rows, buf);
            writeSeparator(cols, buf);
        
            return buf.toString();
        }
    }
    
    private static class NCTestResultImpl implements NCTestResult {
        private String txt;
        private long dsId;
        private String mdlId;
        private String res;
        private String err;
        private long procTime;
        private String valErr;
    
        NCTestResultImpl(String txt, long dsId, String mdlId, String res, String err, long procTime, String valErr) {
            this.txt = txt;
            this.dsId = dsId;
            this.mdlId = mdlId;
            this.res = res;
            this.err = err;
            this.procTime = procTime;
            this.valErr = valErr;
        }
    
        @Override
        public String getText() {
            return txt;
        }
        
        @Override
        public long getProcessingTime() {
            return procTime;
        }
    
        @Override
        public long getDatasourceId() {
            return dsId;
        }
    
        @Override
        public String getModelId() {
            return mdlId;
        }
    
        @Override
        public String getResult() {
            return res;
        }
    
        @Override
        public String getError() {
            return err;
        }
    
        @Override
        public Optional<String> getValidationError() {
            return valErr == null ? Optional.empty() : Optional.of(valErr);
        }
    }
    
    /**
     * Client implementation.
     */
    private class NCTestClientImpl implements NCTestClient {
        private static final String STATUS_API_OK = "API_OK";
        private final Type TYPE_RESP = new TypeToken<HashMap<String, Object>>() {}.getType();
        private final Type TYPE_STATES = new TypeToken<ArrayList<NCRequestStateJson>>() {}.getType();
        private final Type TYPE_DSS = new TypeToken<ArrayList<NCDsJson>>() {}.getType();
    
        private final Gson gson = new Gson();
        
        private final CloseableHttpClient client;
    
        NCTestClientImpl() {
            this.client = mkClient();
        }
        
        private CloseableHttpClient mkClient() {
            return cliSup != null ? cliSup.get() : HttpClients.createDefault();
        }
    
        @SuppressWarnings("unchecked")
        private<T> T getField(Map<String, Object> m, String fn) throws NCTestClientException {
            Object o = m.get(fn);
        
            if (o == null)
                throw new NCTestClientException(
                    String.format("Missed expected field [fields=%s, field=%s]", m.keySet(), fn)
                );
        
            try {
                return (T) o;
            }
            catch (ClassCastException e) {
                throw new NCTestClientException(String.format("Invalid field type: %s", o), e);
            }
        }
    
        private void checkStatus(Map<String, Object> m) throws NCTestClientException {
            String status = getField(m, "status");
        
            if (!status.equals(STATUS_API_OK))
                throw new NCTestClientException("Unexpected message status: " + status);
        }
    
        private <T> T extract(JsonElement js, Type t) throws NCTestClientException {
            try {
                return gson.fromJson(js, t);
            }
            catch (JsonSyntaxException e) {
                throw new NCTestClientException(String.format("Invalid field type [json=%s, type=%s]", js, t), e);
            }
        }
    
        private <T> T extract(String js, String name, Type type) throws NCTestClientException {
            Map<String, Object> m = gson.fromJson(js, TYPE_RESP);
        
            checkStatus(m);
        
            return extract(gson.toJsonTree(getField(m, name)), type);
        }
        
        @SafeVarargs
        private final String post(String url, Pair<String, Object>... ps) throws NCTestClientException, IOException {
            HttpPost post = new HttpPost(url);
        
            try {
                if (reqCfg != null)
                    post.setConfig(reqCfg);
            
                StringEntity entity = new StringEntity(
                    gson.toJson(
                        Arrays.stream(ps).
                            filter(p -> p.getValue() != null).
                            collect(Collectors.toMap(Pair::getKey, Pair::getValue))
                    )
                );
    
                post.setHeader("Content-Type", "application/json");
                post.setEntity(entity);
            
                log.trace("Request prepared: {}", post);
            
                ResponseHandler<String> h = resp -> {
                    int code = resp.getStatusLine().getStatusCode();
                
                    HttpEntity e = resp.getEntity();
                
                    String js = e != null ? EntityUtils.toString(e) : null;
                
                    if (js == null)
                        throw new NCTestClientException(String.format("Unexpected empty response [code=%d]", code));
                
                    switch (code) {
                        case 200: return js;
                        case 400: throw new NCTestClientException(js);
                        default:
                            throw new NCTestClientException(
                                String.format("Unexpected response [code=%d, text=%s]", code, js)
                            );
                    }
                };
            
                String s = client.execute(post, h);
            
                log.trace("Response received: {}", s);
            
                return s;
            }
            finally {
                post.releaseConnection();
            }
        }
    
        @Override
        public List<NCTestResult> test(NCTestSentence... tests) throws NCTestClientException, IOException {
            return test(Arrays.asList(tests));
        }
        
        private <T> void checkDups(
            List<NCTestSentence> tests,
            Function<NCTestSentence, T> extractField,
            String fieldName
        ) {
            List<Pair<String, T>> allTestPairs =
                tests.stream().
                    map(p -> Pair.of(p.getText(), extractField.apply(p))).
                    filter(p -> p.getRight() != null).
                    collect(Collectors.toList());
    
            List<Pair<String, T>> testsPairs = allTestPairs.stream().distinct().collect(Collectors.toList());
    
            if (testsPairs.size() != allTestPairs.size()) {
                allTestPairs.removeAll(testsPairs);
        
                String s =
                    allTestPairs.stream().
                        map(p -> "sentence=" + p.getLeft() + ", " + fieldName + "=" + p.getRight()).
                        collect(Collectors.joining(";", "[", "]"));
        
                throw new NCTestClientException("Sentences texts cannot be duplicated within same " + fieldName + ": " + s);
            }
        }
    
        @Override
        public synchronized List<NCTestResult> test(List<NCTestSentence> tests)
            throws NCTestClientException, IOException {
            checkNotNull("tests", tests);
            
            checkDups(tests, NCTestSentence::getDatasourceId, "datasource");
            checkDups(tests, NCTestSentence::getModelId, "model");
            
            Set<String> mdlIds =
                tests.stream().
                    filter(p -> p.getModelId().isPresent()).
                    map(p -> p.getModelId().get()).
                    collect(Collectors.toSet());
            
            String auth = signin();
    
            Map<String, Long> newDssIds = new HashMap<>();
            
            int num = 0;
            
            for (String mdlId : mdlIds) {
                newDssIds.put(mdlId, createTestDs(auth, mdlId, num++));
            }
    
            List<NCTestResult> res = new ArrayList<>();
            
            try {
                Map<Long, String> dssMdlIds =
                    getDss(auth).stream().collect(Collectors.toMap(NCDsJson::getDatasourceId, NCDsJson::getModelId));
    
                Map<NCTestSentence, Pair<Long, String>> testsExt =
                    tests.stream().collect(
                        Collectors.toMap(
                            p -> p,
                            p -> {
                                long dsId =
                                    p.getDatasourceId().isPresent() ?
                                        p.getDatasourceId().get() :
                                        newDssIds.get(p.getModelId().get());
                                
                                return Pair.of(dsId, dssMdlIds.get(dsId));
                            }
                        )
                    );
                
                Function<NCTestSentence, Map<NCTestSentence, Pair<Long, String>>> mkSingleMap = (t) -> {
                    Map<NCTestSentence, Pair<Long, String>> m = new HashMap<>();
                    
                    m.put(t, testsExt.get(t));
                    
                    return m;
                };
    
                if (clearConv) {
                    for (NCTestSentence test : tests) {
                        clearConversation(auth, testsExt.get(test).getLeft());
        
                        res.addAll(executeAsync(auth, mkSingleMap.apply(test)));
                    }
                }
                else {
                    Set<Long> dsIds = tests.stream().map(t -> testsExt.get(t).getLeft()).collect(Collectors.toSet());
                    
                    if (asyncMode) {
                        clearConversationAllDss(auth, dsIds);
    
                        res.addAll(executeAsync(auth, testsExt));
                    }
                    else {
                        clearConversationAllDss(auth, dsIds);
    
                        for (NCTestSentence test : tests) {
                            res.addAll(executeAsync(auth, mkSingleMap.apply(test)));
                        }
                    }
                }
            }
            catch (InterruptedException e) {
                throw new NCTestClientException("Test interrupted.", e);
            }
            finally {
                // This potential error can be ignored. Also it shouldn't override main method errors.
                try {
                    for (Long id : newDssIds.values()) {
                        deleteTestDs(auth, id);
                    }
                    
                    signout(auth);
                }
                catch (Exception e) {
                    log.error("Signout error.", e);
                }
            }
    
            // TODO:
            // res.sort(Comparator.comparingInt(o -> testsPairs.indexOf(Pair.of(o.getText(), o.getDsId()))));
    
            printResult(tests, res);
    
            return res;
        }
    
        private void printResult(List<NCTestSentence> tests, List<NCTestResult> results) {
            assert tests != null && results != null;
            assert !tests.isEmpty();
            assert tests.size() == results.size();
            
            int n = tests.size();
            
            AsciiTable resTab = new AsciiTable(
                "Sentence",
                "Datasource ID",
                "Model ID",
                "Expected Result",
                "Has checked function",
                "Result",
                "Error",
                "Validation",
                "Processing Time (ms)"
            );
    
            for (int i = 0; i < n; i++) {
                NCTestSentence test = tests.get(i);
                NCTestResult res = results.get(i);
    
                List<Object> row = new ArrayList<>();
    
                row.add(res.getText());
                row.add(res.getDatasourceId());
                row.add(res.getModelId());
                row.add(test.isSuccessful());
                row.add(test.isSuccessful() ? test.getCheckResult().isPresent() : test.getCheckError().isPresent());
                row.add(res.getResult());
                row.add(res.getError());
                row.add(res.getValidationError().isPresent() ? res.getValidationError().get() : "Passed");
                row.add(res.getProcessingTime());
    
                resTab.addRow(row);
            }
    
            log.info("Test result:\n" + resTab.mkContent());
    
            AsciiTable statTab = new AsciiTable(
                "Tests Count",
                "Passed",
                "Failed",
                "Min Processing Time (ms)",
                "Max Processing Time (ms)",
                "Avg Processing Time (ms)"
            );
    
            List<Object> row = new ArrayList<>();
            
            row.add(n);
            
            long passed = results.stream().filter(p -> !p.getValidationError().isPresent()).count();
            
            row.add(passed);
            row.add(n - passed);
    
            OptionalLong min = results.stream().mapToLong(NCTestResult::getProcessingTime).min();
            OptionalLong max = results.stream().mapToLong(NCTestResult::getProcessingTime).max();
            
            assert min.isPresent() && max.isPresent();
            
            row.add(min.getAsLong());
            row.add(max.getAsLong());
            
            double avg = results.stream().mapToDouble(NCTestResult::getProcessingTime).sum() / n;
            
            row.add(Math.round(avg * 100.) / 100.);
    
            statTab.addRow(row);
    
            log.info("Tests statistic:\n" + statTab.mkContent());
        }
    
        private void clearConversationAllDss(String auth, Set<Long> dssIds) throws IOException, NCTestClientException {
            for (Long dsId : dssIds) {
                clearConversation(auth, dsId);
            }
        }
    
        private void clearConversation(String auth, long dsId) throws IOException, NCTestClientException {
            log.info("`clear/conversation` request sent for datasource: {}", dsId);
            
            checkStatus(
                gson.fromJson(
                    post(baseUrl + "clear/conversation",
                        Pair.of("accessToken", auth),
                        Pair.of("dsId", dsId)
                    ),
                    TYPE_RESP
                )
            );
        }
    
        private void cancel(String auth, Set<String> ids) throws IOException, NCTestClientException {
            log.info("`cancel` request sent for requests: {}", ids);
            
            checkStatus(
                gson.fromJson(
                    post(baseUrl + "cancel",
                        Pair.of("accessToken", auth),
                        Pair.of("srvReqIds", ids)
                    ),
                    TYPE_RESP
                )
            );
        }
    
        private long createTestDs(String auth, String mdlId, long num) throws IOException, NCTestClientException {
            log.info("`ds/add` request sent for model: {}", mdlId);
            
            long id =
                extract(
                    post(baseUrl + "ds/add",
                        Pair.of("accessToken", auth),
                        Pair.of("name", "test-" + num),
                        Pair.of("shortDesc", "Test datasource"),
                        Pair.of("mdlId", mdlId),
                        Pair.of("mdlName", "Test model"),
                        Pair.of("mdlVer", "Test version")
            
                    ),
                    "id",
                    Long.class
               );
    
            log.info("Temporary test datasource created: {}", id);
            
            return id;
        }
        
        private void deleteTestDs(String auth, long id) throws IOException, NCTestClientException {
            log.info("`ds/delete` request sent for model: {}", id);
            
            checkStatus(
                gson.fromJson(
                    post(baseUrl + "ds/delete",
                        Pair.of("accessToken", auth),
                        Pair.of("id", id)
                    ),
                    TYPE_RESP
                )
            );
        }
    
        private String signin() throws IOException, NCTestClientException {
            log.info("`user/signin` request sent for: {}", email);
            
            return extract(
                post(
                    baseUrl + "user/signin",
                    Pair.of("email", email),
                    Pair.of("passwd", pswd)
                    
                ),
                "accessToken",
                String.class
            );
        }
    
        private List<NCDsJson> getDss(String auth) throws IOException, NCTestClientException {
            log.info("`ds/all` request sent for: {}", email);
    
            Map<String, Object> m = gson.fromJson(
                post(baseUrl + "ds/all",
                    Pair.of("accessToken", auth)
                ),
                TYPE_RESP
            );
    
            checkStatus(m);
    
            return extract(gson.toJsonTree(getField(m, "dataSources")), TYPE_DSS);
        }
    
        private List<NCRequestStateJson> check(String auth) throws IOException, NCTestClientException {
            log.info("`check` request sent for: {}", email);
        
            Map<String, Object> m = gson.fromJson(
                post(baseUrl + "check",
                    Pair.of("accessToken", auth)
                ),
                TYPE_RESP
            );
        
            checkStatus(m);
        
            return extract(gson.toJsonTree(getField(m, "states")), TYPE_STATES);
        }
    
        private String ask(String auth, String txt, long dsId) throws IOException, NCTestClientException {
            log.info("`ask` request sent: {} to datasource: {}", txt, dsId);
        
            return extract(
                post(baseUrl + "ask",
                    Pair.of("accessToken", auth),
                    Pair.of("txt", txt),
                    Pair.of("dsId", dsId),
                    Pair.of("isTest", true)
                ),
                "srvReqId",
                String.class
            );
        }
    
    
        private void signout(String auth) throws IOException, NCTestClientException {
            log.info("`user/signout` request sent for: {}", email);
            
            checkStatus(
                gson.fromJson(
                    post(baseUrl + "user/signout",
                        Pair.of("accessToken", auth)
                    ),
                    TYPE_RESP
                )
            );
        }
    
        private List<NCTestResult> executeAsync(
            String auth,
            Map<NCTestSentence, Pair<Long, String>> tests
        ) throws IOException, InterruptedException {
            int n = tests.size();
    
            Map<String, NCTestSentence> testsMap = new HashMap<>(n);
            Map<String, NCRequestStateJson> testsResMap = new HashMap<>();
            Map<NCTestSentence, String> askErrTests = new HashMap<>();
            
            try {
                for (Map.Entry<NCTestSentence, Pair<Long, String>> entry : tests.entrySet()) {
                    NCTestSentence test = entry.getKey();
                    Pair<Long, String> ids = entry.getValue();
    
                    try {
                        String srvReqId = ask(auth, test.getText(), ids.getLeft());
        
                        log.debug("Sentence sent: {}", srvReqId);
        
                        testsMap.put(srvReqId, test);
                    }
                    catch (NCTestClientException e) {
                        askErrTests.put(test, e.getMessage());
                    }
                }
    
                log.debug("Sentences sent: {}", testsMap.size());
    
                long startTime = System.currentTimeMillis();
    
                while (testsResMap.size() != testsMap.size()) {
                    if (System.currentTimeMillis() - startTime > maxCheckTime)
                        throw new NCTestClientException(
                            String.format("Timed out waiting for response: %d", maxCheckTime)
                        );
        
                    List<NCRequestStateJson> states = check(auth);
                    
                    Thread.sleep(checkIntervalMs);
    
                    Map<String, NCRequestStateJson> res =
                        states.stream().
                        filter(p -> p.getStatus().equals("QRY_READY")).
                        collect(Collectors.toMap(NCRequestStateJson::getServerRequestId, p -> p));
    
                    testsResMap.putAll(res);
    
                    long newResps = res.keySet().stream().filter(p -> !testsResMap.containsKey(p)).count();
    
                    log.debug("Request processed: {}", newResps);
                }
            }
            finally {
                if (!testsMap.isEmpty())
                    // This potential error can be ignored. Also it shouldn't override main method errors.
                    try {
                        cancel(auth, testsMap.keySet());
                    }
                    catch (Exception e) {
                        log.error("Tests request cancel error: " + testsMap.keySet(), e);
                    }
            }
    
            return Stream.concat(
                testsResMap.entrySet().stream().map(p -> {
                    NCTestSentence test = testsMap.get(p.getKey());
                    NCRequestStateJson testRes = p.getValue();
        
                    return mkResult(test, testRes, tests.get(test).getRight());
                }),
                askErrTests.entrySet().stream().map(p -> {
                    NCTestSentence test = p.getKey();
                    String err = p.getValue();
    
                    Pair<Long, String> ids = tests.get(test);
    
                    return new NCTestResultImpl(test.getText(), ids.getLeft(), ids.getRight(), null, err, 0, "");
                })
            ).collect(Collectors.toList());
        }
    }
    
    private NCTestResult mkResult(NCTestSentence test, NCRequestStateJson testRes, String mdlId) {
        String res = testRes.getResultBody();
        String err = testRes.getError();
        
        if (test.isSuccessful() && testRes.getError() == null && test.getCheckResult().isPresent()) {
            NCQueryResult qRes = new NCQueryResult();

            qRes.setType(testRes.getResultType());
            qRes.setBody(testRes.getResultBody());

            if (!test.getCheckResult().get().test(qRes))
                err = "Check result function invocation was not successful";
        }
        else if (
            !test.isSuccessful() &&
            testRes.getError() != null &&
            test.getCheckError().isPresent() &&
            !test.getCheckError().get().test(err)
        )
            err = "Check error function invocation was not successful";
    
        return new NCTestResultImpl(test.getText(), testRes.getDatasourceId(),mdlId, res, err, testRes.getUpdateTstamp() - testRes.getCreateTstamp(), "");
    }
    
    private static void checkNotNull(String name, Object val) throws IllegalArgumentException {
        if (val == null)
            throw new IllegalArgumentException(String.format("Argument cannot be null: '%s'", name));
    }
    
    private static void checkPositive(String name, long v) throws IllegalArgumentException {
        if (v <= 0)
            throw new IllegalArgumentException(String.format("Argument '%s' must be positive: %d", name, v));
    }
    
    private static void checkNotNegative(String name, long v) throws IllegalArgumentException {
        if (v < 0)
            throw new IllegalArgumentException(String.format("Argument '%s' shouldn't be negative: %d", name, v));
    }
    
    /**
     * Creates new default builder instance.
     *
     * @return Builder instance.
     */
    public static NCTestClientBuilder newBuilder() {
        return new NCTestClientBuilder();
    }
    
    /**
     * Sets HTTP REST client configuration parameters.
     *
     * @param reqCfg HTTP REST client configuration parameters.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder withConfig(RequestConfig reqCfg) {
        checkNotNull("reqCfg", reqCfg);
        
        this.reqCfg = reqCfg;
        
        return this;
    }
    
    /**
     * Sets check result delay value in milliseconds.
     * Default values is {@link NCTestClientBuilder#DFLT_CHECK_INTERVAL_MS}. This value should be changed
     * only in cases when account's usage quota is exceeded.
     *
     * @param checkIntervalMs Delay value in milliseconds.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder withCheckInterval(long checkIntervalMs) {
        checkPositive("checkIntervalMs", checkIntervalMs);
        
        this.checkIntervalMs = checkIntervalMs;
        
        return this;
    }
    
    /**
     * Sets whether or not test sentences will be processed in parallel (async mode) or one by one (sync mode).
     * Note that only synchronous mode make sense when testing with conversation support. Default values
     * is {@link NCTestClientBuilder#DFLT_CLEAR_CONVERSATION}.
     *
     * @param asyncMode {@code true} for asynchronous (parallel) mode, {@code false} for synchronous mode.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder withAsyncMode(boolean asyncMode) {
        this.asyncMode = asyncMode;
        
        return this;
    }
    
    /**
     * Sets whether or not to clear conversation after each test request.
     * Note, that if this flag set as {@code false}, requests always sent in synchronous (one-by-one) mode.
     * Default values is {@link NCTestClientBuilder#DFLT_CLEAR_CONVERSATION}.
     *
     * @param clearConv Whether or not to clear conversation after each test request.
     * @return Builder instance for chaining calls.
     */
    public NCTestClientBuilder withClearConversation(boolean clearConv) {
        this.clearConv = clearConv;
        
        return this;
    }
    
    public NCTestClientBuilder withHttpClientSupplier(Supplier<CloseableHttpClient> cliSup) {
        checkNotNull("cliSup", cliSup);
        
        this.cliSup = cliSup;
    
        return this;
    }
    
    public NCTestClientBuilder withBaseUrl(String baseUrl) {
        checkNotNull("baseUrl", baseUrl);
        
        this.baseUrl = baseUrl;
    
        return this;
    }
    
    public NCTestClientBuilder withEmail(String email) {
        checkNotNull("email", email);
        
        this.email = email;
    
        return this;
    }
    
    public NCTestClientBuilder withPassword(String pswd) {
        checkNotNull("pswd", pswd);
        
        this.pswd = pswd;
        
        return this;
    }
    
    public NCTestClientBuilder withMaxCheckTime(long maxCheckTime) {
        checkPositive("maxCheckTime", maxCheckTime);
        
        this.maxCheckTime = maxCheckTime;
    
        return this;
    }
    
    /**
     * Build new configured test client instance.
     *
     * @return Newly built test client instance.
     */
    public NCTestClient build() {
        return new NCTestClientImpl();
    }
}
