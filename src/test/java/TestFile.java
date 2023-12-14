import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

// {
//     '19042|Monica Wilcox|05/04/61',
//     '93388|Isabelle Lowery|12/18/43',
//     '29186|Aron Stevens|12/27/93',
//     '12345|Hilda Schrader Whitcher|07/13/85',
//     '33085|Keyaan Carey|05/23/83',
//     '74822|Delilah Barker|05/12/98',
//     '32345|Hope Kent|06/20/42',
//     '37869|Ameer Russo|06/06/77',
//     '73945|Lillian Mccann|10/25/67',
//     '95256|Casey Finley|11/02/92',
//     '30671|Jason Watson|02/12/72'
// }

//to run, go to directory of this file
//then in console: java TestFile.java

public class TestFile {
  public static void main(String[] args) {
    //var test = "Monica Wilco";
    
    // int test = 15897;
    // var test_str = String.valueOf(test);
    // var strr = test_str.replaceAll("[0-9]","1");
    // int str = Integer.valueOf(strr);

    // Date todaysDate = new Date();
    // DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    // Calendar cal = Calendar.getInstance();
    // cal.setTime(todaysDate);
    // cal.add(Calendar.DATE, 2);
    // String str = df.format(cal.getTime());

    // Double test = 123.123;
    // String str1 = Double.toString(test).replaceAll("[0-9]","4");
    // Double str = Double.valueOf(str1)+1.0;

    Long test = 15000000000L;
    String l = Long.toString(test).replaceAll("[0-9]","5");
    Long str = Long.parseLong(l);
    
    System.out.println(str+1);
  }
}