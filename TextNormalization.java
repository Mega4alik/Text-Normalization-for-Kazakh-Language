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


import java.io.FileInputStream;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.util.InvalidFormatException;
public class TextNormalization {    
    static int patterns_n = 4;
    static ArrayList<rule_structure>[] patterns = new ArrayList[101];
    static HashMap<String,String> abbrs = new HashMap<String,String>();
    static {
        abbrs.put("ҚР","Қазақстан Республикасы");
        abbrs.put("ҚТЖ","Қазақстан Темір жолы");
    }
    public static void main(String[] args) throws Exception {
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
      //ҚР
      patterns[4] = new ArrayList<rule_structure>();
      patterns[4].add(new rule_structure(new int[]{2},1,99));
      //endOf Ini patterns            
      tokenize();
     }
    
     public static void tokenize() {                    
      String text = "ҚР 28-ші ата-анасы келді. Сосын ҚТЖ 87078940178 номеріне 3458 рет звондады";
      String[] sentences = SentenceDetect(text);
            
      for (String sentence:sentences) {
      String ans = ""; 
      List a = get_tokens(sentence);
      for (int i=0;i<a.size();i++) {
          String token = a.get(i).toString();          
          int j = get_pattern(token);               
          //System.out.println(j+" "+token+" ");
          ans+=normalize(token,j)+" ";                                               
      }            
      //System.out.println(a.toString());      
      System.out.println(ans);
      }
      
     }
     
     public static int get_pattern(String st){         
         int len = 0;
         int[] a = new int[101];         
         for (int i=0;i<st.length();i++) {
             char ch = st.charAt(i);
             int k=getK(ch);                          
             if (k>0) {             
                 len++;
                 a[len] = k;             
             }
         }
                 
         //getting pattern
         for (int i=1;i<=patterns_n;i++) 
             recognize: {
              int curr_len=0,j2=0,j=1,curr_pattern_len = patterns[i].size();                           
              while (j<=len) {                  
                  int k = a[j];                  
                  int lf = patterns[i].get(j2).length_from, lt = patterns[i].get(j2).length_to;
                  ArrayList<Integer> set = patterns[i].get(j2).set;
                  
                 if (set.contains(k) && curr_len+1<=lt) {curr_len++;j++;}                                   
                 else if (!set.contains(k)) {
                     if (curr_len < lf) break recognize;
                     j2++;
                     if (j2==curr_pattern_len) break recognize;
                     curr_len=0;
                 }
                 else if (curr_len+1>lt) break recognize;
                 else break recognize;                 
              }              
              if (len==0 || j2!=curr_pattern_len-1) break recognize;
              return i;                           
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
          case 4:{
              if (abbrs.containsKey(st)) ans = abbrs.get(st); 
              else ans = st;
              break;
          }
        }
         return ans.toLowerCase();    
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
                int len = tokens.size();
                for (int i=0;i<len;i++) 
                    if (i<len-2 && get_pattern(tokens.get(i)+tokens.get(i+1)+tokens.get(i+2))==2) {
                        ans.add(tokens.get(i)+tokens.get(i+1)+tokens.get(i+2));
                        i+=2;
                    } else ans.add(tokens.get(i));
                        
                return ans;
        }
    
 public static String[] SentenceDetect(String paragraph)  {	 
	// always start with a model, a model is learned from training data
        String sentences[] = null;
        try {
	InputStream is = new FileInputStream("en-sent.bin");
	SentenceModel model = new SentenceModel(is);
	SentenceDetectorME sdetector = new SentenceDetectorME(model);
 
	sentences = sdetector.sentDetect(paragraph);
        is.close();        
        } catch(Exception e){e.printStackTrace();}
	
        return sentences;
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
