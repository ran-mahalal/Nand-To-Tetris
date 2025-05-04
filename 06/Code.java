public class Code {
        //holds an instance of a code - for the use as a singelton

    private static Code instance;
    
    private Code() {
    }
    //constructor
    public static Code getInstance() {
        if (instance == null) {
            instance = new Code();
        }
        return instance;
    }

    public String dest(String destString) {
        if (destString == null || destString.isEmpty()) return "000";
        //lights the bit, if the right char is in the string
        StringBuilder destBinary = new StringBuilder("000");
        if (destString.contains("A")) destBinary.setCharAt(0, '1');
        if (destString.contains("D")) destBinary.setCharAt(1, '1');
        if (destString.contains("M")) destBinary.setCharAt(2, '1');
        
        return destBinary.toString();
    }

    public String comp(String compString) {
        if (compString == null || compString.isEmpty()) {
            throw new IllegalArgumentException("Comp compString cannot be null or empty");
        }

        // trims the string - just in case
        String trimmedCompString = compString.trim();
        
        //determine the binary representation
        if (trimmedCompString.equals("0")) {
            return "0101010";
        } else if (trimmedCompString.equals("1")) {
            return "0111111";
        } else if (trimmedCompString.equals("-1")) {
            return "0111010";
        } else if (trimmedCompString.equals("D")) {
            return "0001100";
        } else if (trimmedCompString.equals("A")) {
            return "0110000";
        } else if (trimmedCompString.equals("!D")) {
            return "0001101";
        } else if (trimmedCompString.equals("!A")) {
            return "0110001";
        } else if (trimmedCompString.equals("-D")) {
            return "0001111";
        } else if (trimmedCompString.equals("-A")) {
            return "0110011";
        } else if (trimmedCompString.equals("D+1")) {
            return "0011111";
        } else if (trimmedCompString.equals("A+1")) {
            return "0110111";
        } else if (trimmedCompString.equals("D-1")) {
            return "0001110";
        } else if (trimmedCompString.equals("A-1")) {
            return "0110010";
        } else if (trimmedCompString.equals("D+A")) {
            return "0000010";
        } else if (trimmedCompString.equals("D-A")) {
            return "0010011";
        } else if (trimmedCompString.equals("A-D")) {
            return "0000111";
        } else if (trimmedCompString.equals("D&A")) {
            return "0000000";
        } else if (trimmedCompString.equals("D|A")) {
            return "0010101";
        } else if (trimmedCompString.equals("M")) {
            return "1110000";
        } else if (trimmedCompString.equals("!M")) {
            return "1110001";
        } else if (trimmedCompString.equals("-M")) {
            return "1110011";
        } else if (trimmedCompString.equals("M+1")) {
            return "1110111";
        } else if (trimmedCompString.equals("M-1")) {
            return "1110010";
        } else if (trimmedCompString.equals("D+M")) {
            return "1000010";
        } else if (trimmedCompString.equals("D-M")) {
            return "1010011";
        } else if (trimmedCompString.equals("M-D")) {
            return "1000111";
        } else if (trimmedCompString.equals("D&M")) {
            return "1000000";
        } else if (trimmedCompString.equals("D|M")) {
            return "1010101";
        } else {
            throw new IllegalArgumentException("Invalid comp compString: " + compString);
        }
    }


    public String jump(String jumpString) {
        if (jumpString == null || jumpString.isEmpty()) {
            //jumpstring can be null and in this case returns 000
            return "000";
        }
        // trim the string - just in case
        String trimmedJumpString = jumpString.trim();

        // determine the  binary representation
        if (trimmedJumpString.equals("JGT")) {
            return "001";
        } else if (trimmedJumpString.equals("JEQ")) {
            return "010";
        } else if (trimmedJumpString.equals("JGE")) {
            return "011";
        } else if (trimmedJumpString.equals("JLT")) {
            return "100";
        } else if (trimmedJumpString.equals("JNE")) {
            return "101";
        } else if (trimmedJumpString.equals("JLE")) {
            return "110";
        } else if (trimmedJumpString.equals("JMP")) {
            return "111";
        } else {
            //defaault, shouldn't get here
            return "000";
        }
    }
}