package io.github.malys23.reorderiq_api.seed;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DevDataLoader implements ApplicationRunner{
    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);

    private final String dataDir;
    private final List<SeedProperties.DemoUser> users;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;
    public DevDataLoader(SeedProperties properties, PasswordEncoder passwordEncoder, JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate){
        this.dataDir = properties.dataDir();
        this.users = properties.users();
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer existing = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM items", Integer.class);
        if(existing >0){
            log.info("Seed data already present, skipping");
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {
            Path path = Path.of(dataDir);
            log.info("Seed folder {} exists: {}", path.toAbsolutePath(), Files.exists(path));

            // demo users: build the rows
            List<Object[]> userRows = new ArrayList<>();
            for (SeedProperties.DemoUser user : users) {
                Object[] row = new Object[]{
                        user.id(),
                        user.username(),
                        passwordEncoder.encode(user.password()),
                        user.role()
                };
                userRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO users (user_id, username, password_hash, user_role) VALUES (?, ?, ?, ?)", userRows);
            log.info("Loaded {} demo users", userRows.size());

            //build suppliers
            List<CSVRecord> suppliers = LoaderHelper.readCsv(path.resolve("suppliers.csv"));
            List<Object[]> supplierRows = new ArrayList<>();
            for (CSVRecord supplier : suppliers) {
                Object[] row = new Object[]{
                        LoaderHelper.textToInteger(supplier, "supplier_id"),
                        LoaderHelper.cellContent(supplier, "name"),
                        LoaderHelper.cellContent(supplier, "category"),
                        LoaderHelper.textToBigDecimal(supplier, "lead_time_cv"),
                        LoaderHelper.textToBigDecimal(supplier, "on_time_rate")
                };
                supplierRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO suppliers(supplier_id, name, category, lead_time_cv, on_time_rate) " +
                    "VALUES (?, ?, ?, ?, ?)", supplierRows);
            log.info("suppliers.csv: {} rows loaded", supplierRows.size());

            //build items
            List<CSVRecord> items = LoaderHelper.readCsv(path.resolve("items.csv"));
            List<Object[]> itemRows = new ArrayList<>();
            for (CSVRecord item : items) {
                Object[] row = new Object[]{
                        LoaderHelper.textToInteger(item, "item_id"),
                        LoaderHelper.cellContent(item, "name"),
                        LoaderHelper.cellContent(item, "category"),
                        LoaderHelper.textToInteger(item, "supplier_id"),
                        LoaderHelper.textToBigDecimal(item, "unit_cost"),
                        LoaderHelper.textToInteger(item, "lead_time_days"),
                        LoaderHelper.cellContent(item, "abc_class")
                };
                itemRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO items(item_id, name, category, supplier_id, unit_cost, " +
                    "lead_time_days, abc_class) VALUES (?, ?, ?, ?, ?, ?, ?)", itemRows);
            log.info("items.csv: {} rows loaded", itemRows.size());

            //build stock levels
            List<CSVRecord> stock = LoaderHelper.readCsv(path.resolve("stock_levels.csv"));
            List<Object[]> stockRows = new ArrayList<>();
            for (CSVRecord s : stock) {
                Object[] row = new Object[]{
                        LoaderHelper.textToInteger(s, "item_id"),
                        LoaderHelper.textToInteger(s, "on_hand"),
                        LoaderHelper.textToInteger(s, "on_order"),
                };
                stockRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO stock_levels(item_id, on_hand, on_order) VALUES (?, ?, ?)", stockRows);
            log.info("stock_levels.csv: {} rows loaded", stockRows.size());

            //build demand history
            List<CSVRecord> demand = LoaderHelper.readCsv(path.resolve("demand_history.csv"));
            List<Object[]> demandRows = new ArrayList<>();
            for (CSVRecord d : demand) {
                Object[] row = new Object[]{
                        LoaderHelper.textToInteger(d, "item_id"),
                        LoaderHelper.textToDate(d, "week_start"),
                        LoaderHelper.textToInteger(d, "quantity"),
                        LoaderHelper.textToBoolean(d, "is_holdout")
                };
                demandRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO demand_history(item_id, week_start, quantity, " +
                    "is_holdout) VALUES (?, ?, ?, ?)", demandRows);
            log.info("demand_history.csv: {} rows loaded", demandRows.size());

            //build purchase requests
            List<CSVRecord> prs = LoaderHelper.readCsv(path.resolve("purchase_requests.csv"));
            List<Object[]> prRows = new ArrayList<>();
            for (CSVRecord pr : prs) {
                Object[] row = new Object[]{
                        LoaderHelper.textToInteger(pr, "request_id"),
                        LoaderHelper.textToInteger(pr, "item_id"),
                        LoaderHelper.textToInteger(pr, "quantity"),
                        LoaderHelper.textToDate(pr, "needed_by"),
                        LoaderHelper.textToDate(pr, "created_at"),
                        LoaderHelper.textToInteger(pr, "requested_by"),
                        LoaderHelper.cellContent(pr, "status"),
                        LoaderHelper.textToDate(pr, "submitted_at"),
                        LoaderHelper.textToDate(pr, "approved_at"),
                        LoaderHelper.textToNullableInteger(pr, "approved_by"),
                        LoaderHelper.textToDate(pr, "ordered_at"),
                        LoaderHelper.textToDate(pr, "received_at"),
                        LoaderHelper.textToNullableInteger(pr, "received_by"),
                };
                prRows.add(row);
            }
            jdbcTemplate.batchUpdate("INSERT INTO purchase_requests(request_id, item_id, quantity, needed_by, " +
                    "created_at, requested_by, status, submitted_at, approved_at, approved_by, ordered_at, received_at, " +
                    "received_by) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)", prRows);
            log.info("purchase_requests.csv: {} rows loaded", prRows.size());

            jdbcTemplate.execute(
                    "SELECT setval(pg_get_serial_sequence('purchase_requests', 'request_id'), " +
                            "(SELECT COALESCE(MAX(request_id), 1) FROM purchase_requests))");
            log.info("Seed data loaded");
        });
    }
}
