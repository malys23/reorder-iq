package io.github.malys23.reorderiq_api.seed;

import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("dev")
public class DevDataLoader implements ApplicationRunner{
    private static final Logger log = LoggerFactory.getLogger(DevDataLoader.class);

    private final String dataDir;
    private final List<SeedProperties.DemoUser> users;
    private final PasswordEncoder passwordEncoder;
    public DevDataLoader(SeedProperties properties, PasswordEncoder passwordEncoder){
        this.dataDir = properties.dataDir();
        this.users = properties.users();
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Path path = Path.of(dataDir);
        log.info("Seed folder {} exists: {}", path.toAbsolutePath(), Files.exists(path));

        // demo users: build the rows
        List<Object[]> userRows = new ArrayList<>();
        for (SeedProperties.DemoUser user: users){
            Object[] row = new Object[]{
                    user.id(),
                    user.username(),
                    passwordEncoder.encode(user.password()),
                    user.role()
            };
            userRows.add(row);
        }
        log.info("Prepared {} demo users", userRows.size());

        //demo csv load
        List<CSVRecord> suppliers = LoaderHelper.readCsv(path.resolve("suppliers.csv"));
        log.info("suppliers.csv: {} rows", suppliers.size());

        List<CSVRecord> items = LoaderHelper.readCsv(path.resolve("items.csv"));
        log.info("items.csv: {} rows", items.size());

        List<CSVRecord> stock = LoaderHelper.readCsv(path.resolve("stock_levels.csv"));
        log.info("stock_levels.csv: {} rows", stock.size());

        List<CSVRecord> demand = LoaderHelper.readCsv(path.resolve("demand_history.csv"));
        log.info("demand_history.csv: {} rows", demand.size());
        int counter1 = 0;
        for (CSVRecord n: demand){
            try{
                Integer itemId = LoaderHelper.textToInteger(n, "item_id");
                LocalDate weekStart = LoaderHelper.textToDate(n, "week_start");
                Boolean isHoldout = LoaderHelper.textToBoolean(n, "is_holdout");
                if (isHoldout) {counter1 += 1;}
            } catch (RuntimeException e){
                throw new IllegalStateException(
                        "demand_history.csv: bad data in row " + n.getRecordNumber(), e);
            }
        }
        log.info("demand_history.csv: {} is_holdout true", counter1);

        List<CSVRecord> prs = LoaderHelper.readCsv(path.resolve("purchase_requests.csv"));
        log.info("purchase_requests.csv: {} rows", prs.size());
        int counter2 = 0;
        for (CSVRecord pr : prs){
            try{
                Integer itemId = LoaderHelper.textToInteger(pr, "item_id");
                Integer quantity = LoaderHelper.textToInteger(pr, "quantity");
                LocalDate neededBy = LoaderHelper.textToDate(pr, "needed_by");
                LocalDate createdAt = LoaderHelper.textToDate(pr, "created_at");
                Integer requestedBy = LoaderHelper.textToInteger(pr, "requested_by");
                String status = pr.get("status");
                LocalDate submittedAt = LoaderHelper.textToDate(pr, "submitted_at");
                LocalDate approvedAt = LoaderHelper.textToDate(pr, "approved_at");
                Integer approvedBy = LoaderHelper.textToNullableInteger(pr, "approved_by");
                LocalDate orderedAt = LoaderHelper.textToDate(pr, "ordered_at");
                LocalDate receivedAt = LoaderHelper.textToDate(pr, "received_at");
                if (receivedAt != null){ counter2 +=1; }
                Integer receivedBy = LoaderHelper.textToNullableInteger(pr, "received_by");
            } catch (RuntimeException e){
                throw new IllegalStateException(
                        "purchase_requests.csv: bad data in row " + pr.getRecordNumber(), e);
            }
        }
        log.info("purchase_requests.csv: {} received requests", counter2);
    }
}
