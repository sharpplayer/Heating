import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Decoder {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub
    //2.94.8a.uX^485.^0^0[z^1]I^1^(^$^,S^)d.83.
    // 2.94.8a.uX485.^0^0[z1]I1^(^$,^S)0d.83.
    //02.94.8A.uX885.^0^0[z1]I1^(^$,^S)0D.83.
    BufferedReader in;
    
    try {
      in = new BufferedReader(new FileReader(new File("get_current.log")));
      String data;
      int ind;
      String[] codes;
      String bytes;
      String dir = "Unknown";
      boolean newline = true;

      while ((data = in.readLine()) != null) {
        if (data.contains("WRITE"))
          dir = "Write:";
        else if (data.contains("READ"))
          dir = "Read: ";

        if ((ind = data.indexOf(" Length")) != -1) {
          ind = data.indexOf(":", ind) + 1;
          if (ind != 0) {
            bytes = data.substring(ind);
            codes = bytes.split(" ");
            for (int loop = 0; loop < codes.length; loop++) {
              if (codes[loop].length() > 0) {
                if (newline) {
                  System.out.print(dir);
                  newline = false;
                }
                int b = Integer.parseInt(codes[loop], 16);
                int c = b;

                if (b >= 32 && b <= 127)
                  System.out.print((char) b);
                else if ((b >= 160) && (b < 250)) {
                  c = b - 128;
                  System.out.print("^");
                  System.out.print((char) c);
                } else
                  System.out.print(codes[loop] + ".");
                if (b == 131) {
                  System.out.println();
                  newline = true;
                }
              }
            }
          }
        }

      }

    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
