import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

record BankRecords(Collection<Owner> owners, Collection<Account> accounts, Collection<RegisterEntry> registerEntries) { }

public class Obfuscator {
    private static Logger logger = LogManager.getLogger(Obfuscator.class.getName());

    public BankRecords obfuscate(BankRecords rawObjects) {
        //obfuscate owners
        List<Owner> newOwners = new ArrayList<>();
        for (Owner o : rawObjects.owners()) {
            String new_ssn = "***-**-" + o.ssn().substring(7); //SSN
            String new_name = o.name().replaceAll("[a-z]", "x"); //Name

            //ID
            String temp_id = String.valueOf(o.id()).replaceAll("[0-9]","1"); 
            int new_id = Integer.valueOf(temp_id); 

            //DOB
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Calendar cal = Calendar.getInstance();
            cal.setTime(o.dob());
            cal.add(Calendar.DATE, 8); //add 8 days
            Date new_dob = cal.getTime();
          
            String new_address = o.address().replaceAll("[a-z]", "x"); //Address
            String new_address2 = o.address().replaceAll("[a-z]", "x"); //Address2
            String new_city = o.city().replaceAll("[a-z]", "x"); //City
            String new_state = o.state().toLowerCase().replaceAll("[a-z]", "x"); //State
            String new_zip = o.zip().replaceAll("[0-9]", "3"); //Zip
          
            newOwners.add(new Owner(new_name, new_id, new_dob, new_ssn, new_address, new_address2, new_city, new_state, new_zip));
        }
        Collection<Owner> obfuscatedOwners = newOwners;

      
        //obfuscate accounts
        List<Account> newAccounts = new ArrayList<>();
        for (Account a : rawObjects.accounts()) {
            String new_name = a.getName().replaceAll("[a-z]", "x"); //Name

            //ID
            String temp_id = String.valueOf(a.getId()).replaceAll("[0-9]","1");
            int new_id = Integer.valueOf(temp_id);

            //Balance
            String temp_balance = Double.toString(a.getBalance()).replaceAll("[0-9]","4");;
            Double new_balance = Double.valueOf(temp_balance);

            //ownerId
            String temp_ownerId = Long.toString(a.getOwnerId()).replaceAll("[0-9]","5");
            Long new_ownerId = Long.parseLong(temp_ownerId);

            newAccounts.add(new Account(new_name, new_id, new_balance, new_ownerId){ @Override public void monthEnd() {}});
        }
        Collection<Account> obfuscatedAccounts = newAccounts;


        //obfuscate register entries
        List<RegisterEntry> newRegisterEntries = new ArrayList<>();
        for (RegisterEntry r : rawObjects.registerEntries()) {
          //Long id
          String temp_id = Long.toString(r.id()).replaceAll("[0-9]","5");
          Long new_id = Long.parseLong(temp_id);
          
          //long accountId
          String temp_accountId = Long.toString(r.accountId()).replaceAll("[0-9]","5");
          Long new_accountId = Long.parseLong(temp_accountId);
          
          //String entryName
          String new_entryName = r.entryName().replaceAll("[a-z]", "x");
          
          //Double amount
          String temp_amount = Double.toString(r.amount()).replaceAll("[0-9]","4");;
          Double new_amount = Double.valueOf(temp_amount);
          
          //Date date
          DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
          Calendar cal = Calendar.getInstance();
          cal.setTime(r.date());
          cal.add(Calendar.DATE, 8); //add 8 days
          Date new_date = cal.getTime();

          newRegisterEntries.add(new RegisterEntry(new_id, new_accountId, new_entryName, new_amount, new_date));
        }
        Collection<RegisterEntry> obfuscatedRegisterEntries = newRegisterEntries;

        return new BankRecords(obfuscatedOwners, obfuscatedAccounts, obfuscatedRegisterEntries);
    }

    /**
     * Change the integration test suite to point to our obfuscated production
     * records.
     *
     * To use the original integration test suite files run
     *   "git checkout -- src/test/resources/persister_integ.properties"
     */
    public void updateIntegProperties() throws IOException {
        Properties props = new Properties();
        File propsFile = new File("src/test/resources/persister_integ.properties".replace('/', File.separatorChar));
        if (! propsFile.exists() || !propsFile.canWrite()) {
            throw new RuntimeException("Properties file must exist and be writable: " + propsFile);
        }
        try (InputStream propsStream = new FileInputStream(propsFile)) {
            props.load(propsStream);
        }
        props.setProperty("persisted.suffix", "_prod");
        logger.info("Updating properties file '{}'", propsFile);
        try (OutputStream propsStream = new FileOutputStream(propsFile)) {
            String comment = String.format(
                    "Note: Don't check in changes to this file!!\n" +
                    "#Modified by %s\n" +
                    "#to reset run 'git checkout -- %s'",
                    this.getClass().getName(), propsFile);
            props.store(propsStream, comment);
        }
    }

    public static void main(String[] args) throws Exception {
        // enable assertions
        Obfuscator.class.getClassLoader().setClassAssertionStatus("Obfuscator", true);
        logger.info("Loading Production Records");
        Persister.setPersisterPropertiesFile("persister_prod.properties");
        Bank bank = new Bank();
        bank.loadAllRecords();

        logger.info("Obfuscating records");
        Obfuscator obfuscator = new Obfuscator();
        // Make a copy of original values so we can compare length
        // deep-copy collections so changes in obfuscator don't impact originals
        BankRecords originalRecords = new BankRecords(
                new ArrayList<>(bank.getAllOwners()),
                new ArrayList<>(bank.getAllAccounts()),
                new ArrayList<>(bank.getAllRegisterEntries()));
        BankRecords obfuscatedRecords = obfuscator.obfuscate(originalRecords);

        logger.info("Saving obfuscated records");
        obfuscator.updateIntegProperties();
        Persister.resetPersistedFileNameAndDir();
        Persister.setPersisterPropertiesFile("persister_integ.properties");
        // old version of file is cached so we need to override prefix (b/c file changed
        // is not the one on classpath)
        Persister.setPersistedFileSuffix("_prod");
        // writeReords is cribbed from Bank.saveALlRecords(), refactor into common
        // method?
        Persister.writeRecordsToCsv(obfuscatedRecords.owners(), "owners");
        Map<Class<? extends Account>, List<Account>> splitAccounts = obfuscatedRecords
                .accounts()
                .stream()
                .collect(Collectors.groupingBy(rec -> rec.getClass()));
        Persister.writeRecordsToCsv(splitAccounts.get(SavingsAccount.class), "savings");
        Persister.writeRecordsToCsv(splitAccounts.get(CheckingAccount.class),"checking");
        Persister.writeRecordsToCsv(obfuscatedRecords.registerEntries(), "register");

        logger.info("Original   record counts: {} owners, {} accounts, {} registers",
                originalRecords.owners().size(),
                originalRecords.accounts().size(),
                originalRecords.registerEntries().size());
        logger.info("Obfuscated record counts: {} owners, {} accounts, {} registers",
                obfuscatedRecords.owners().size(),
                obfuscatedRecords.accounts().size(),
                obfuscatedRecords.registerEntries().size());

        if (obfuscatedRecords.owners().size() != originalRecords.owners().size())
            throw new AssertionError("Owners count mismatch");
        if (obfuscatedRecords.accounts().size() != originalRecords.accounts().size())
            throw new AssertionError("Account count mismatch");
        if (obfuscatedRecords.registerEntries().size() != originalRecords.registerEntries().size())
            throw new AssertionError("RegisterEntries count mismatch");
    }
}
