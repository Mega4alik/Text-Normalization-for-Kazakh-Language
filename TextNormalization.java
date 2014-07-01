/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package textnormalization;

/**
 *
 * @author YourFlavor-CEO
 */
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import java.text.BreakIterator;
import java.util.ArrayList;
public class TextNormalization {
    //static char letters[] = new char[]{'а','ә','б',}
    static int patterns_n = 3;
    static ArrayList<rule_structure>[] patterns = new ArrayList[101];
    
    public static void main(String[] args) throws Exception{
      //Ini patterns
      //number 1,2,1232132132  
      patterns[1] = new ArrayList<rule_structure>();
      patterns[1].add(new rule_structure(new int[]{3},1,99)); 
      //12-шы
      patterns[2] = new ArrayList<rule_structure>();
      patterns[2].add(new rule_structure(new int[]{3},1,5)); 
      patterns[2].add(new rule_structure(new int[]{4},1,1)); 
      patterns[2].add(new rule_structure(new int[]{1},1,10)); 
      //ана-мен
      patterns[3] = new ArrayList<rule_structure>();
      patterns[3].add(new rule_structure(new int[]{1,2},1,99));
      patterns[3].add(new rule_structure(new int[]{4},1,1));
      patterns[3].add(new rule_structure(new int[]{1,2},1,99));
      //endOf Ini patterns            
      tokenize();
     }
    
     public static void tokenize() {         
      String ans = "";
      List a = get_tokens("12-ші ата-ана,  Осы 8(707)123123203. телефон");
      for (int i=0;i<a.size();i++) {
          String token = a.get(i).toString();          
          int j = get_pattern(token);               
          ans+=normalize(token,j)+" ";                                     
          //System.out.println(j+" "+token+" "+normalize(token,j));
      }
      
      
      System.out.println(a.toString());      
      System.out.println(ans);
     }
     
     public static int get_pattern(String st){         
         int len = 0,last_k=0;
         int[] a = new int[10],b = new int[10];
         
         for (int i=0;i<st.length();i++) {
             char ch = st.charAt(i);
             int k=getK(ch);                          
             if (k>0) {
              if (last_k!=k) {
                 len++;
                 a[len] = k;
                 b[len] = 1;
              } else {
                  b[len]++;
              }
              last_k = k;
             }
         }
        
         //getting pattern
         for (int i=1;i<=patterns_n;i++) 
             recognize: {
             if (patterns[i].size()==len) {              
              for (int j=1;j<=len;j++)
                 if (!(patterns[i].get(j-1).set.contains(a[j]) && (b[j]>=patterns[i].get(j-1).length_from && b[j]<=patterns[i].get(j-1).length_to)))
                      break recognize;                 
              return i;
              }
             
             }
         
         //endOf getting pattern
         return -1;
     }
    
     
     public static String normalize(String st,int pid){
        String ans = "";
        switch(pid) {
          case -1:{
              ans = getNormToken(st);
              break;
          }
          case 1:{ //number
             if (st.length()>6) {
                 int step_len = 2,i=0;
                 if (st.length()%2==1) step_len = 3;
                 while(i<st.length()-1) {
                     ans+=get_NumberNorm(st.substring(i, i+step_len))+" ";
                     i+=step_len;
                     step_len = 2;
                 }
             } else ans = get_NumberNorm(st);   
             break;
          }
          case 2:{
             String[] a = st.split("-");
             ans = get_NumberNorm(a[0])+"н"+a[1];        
             break;
          }
          case 3:{
              String[] a = st.split("-");              
              ans = getNormToken(a[0])+" "+getNormToken(a[1]);              
              break;
          }
        }
         return ans;    
     }
     
     //
     static String get_NumberNorm(String st) {
      String ans = "";int len = st.length(),z=0;
      String[] a1 = new String[]{"","бір","екі","үш","төрт","бес","алты","жеті","сегіз","тоғыз"};
      String[] a2 = new String[]{"","он","жиырма","отыз","қырық","елу","алпыс","жетпіс","сексен","тоқсан"};
      for (int i=0;i<len;i++) {
          int k = Integer.parseInt(st.charAt(len-1-i)+"");
          z++;
          switch(z) {
              case 1: ans = a1[k];break;
              case 2: ans = a2[k]+" "+ans;break;
              case 3: ans = a1[k]+" жүз "+ans;
          }
      }
      int i=0;
      while (i<st.length() && st.charAt(i)=='0') {ans = "ноль "+ans;i++;}
      return ans;
     }
     //

     
     static String getNormToken(String st){
         String ans = "";
         for (int i=0;i<st.length();i++)
             if (getK(st.charAt(i))>0) ans+=st.charAt(i);
         return ans;
     }
     static int getK(char ch){
         int k = 0;
          if (isLetter(ch, "lower")) k=1; else
             if (isLetter(ch, "upper")) k=2; else
             if (ch>='0' && ch<='9') k=3; else
             if (ch=='-') k=4;       
          return k;
     }
     
     public static boolean isLetter(char ch,String mode) {
         char[] a = new char[]{'а','ә','б','в','г','ғ','д','е','ж','з','и','й','к','қ','л','м','н','ң','о','ө','п','р','с','т','у','ұ','ү','ф','х','һ','ц','ч','ш','щ','ъ','ы','і','ь','э','ю','я'};
         if (mode.equals("lower")) {
             for (char ch2:a) if (ch2==ch) return true;
         } else if (mode.equals("upper")) {             
           for (char ch2:a) if (Character.toUpperCase(ch2)==ch) return true;
         }
         return false;
     }
     public static List<String> get_tokens(
                        final String aString)
        {
                List<String> tokens = new ArrayList<String>();
                BreakIterator bi = BreakIterator.getWordInstance();
                bi.setText(aString);
                int begin = bi.first();
                int end;
                for (end = bi.next(); end != BreakIterator.DONE; end = bi.next()) {
                        String t = aString.substring(begin, end);
                        if (t.trim().length() > 0) {
                                tokens.add(aString.substring(begin, end));
                        }
                        begin = end;
                }
                if (end != -1) {
                        tokens.add(aString.substring(end));
                }
                
                //Ans
                ArrayList<String> ans = new ArrayList<String>();
                for (int i=0;i<tokens.size()-2;i++) 
                    if (get_pattern(tokens.get(i)+tokens.get(i+1)+tokens.get(i+2))==2) {
                        ans.add(tokens.get(i)+tokens.get(i+1)+tokens.get(i+2));
                        i+=2;
                    } else ans.add(tokens.get(i));
                        
                return ans;
        }
    }


class rule_structure{
    ArrayList<Integer> set = new ArrayList<Integer>();
    int length_from=0,length_to=0;
    public rule_structure(int[] set_curr,int lf_curr,int lr_curr){
        for (int i=0;i<set_curr.length;i++) set.add(set_curr[i]);
        length_from = lf_curr;
        length_to = lr_curr;
    }
}
