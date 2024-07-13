package com.rodz.adimoshopping;

public class Response {
    public boolean status = true;
    public String error = null;
    public String type = null;
    public String str = null;
    protected String[] chars = null;
    public final String NUMERIC = "numeric";
    public final String ASSOC = "assoc";
    public final String MULTI_NUMERIC = "multi_numeric";
    public final String MULTI_ASSOC = "multi_assoc";
    public final String MULTI_SERIES = "multi_series";
    public String[] keys = null;
    public int rows = 0;
    public String[] series = null;
    
    public class Row{
        public String type;
        public String text;
        
        public Row(String text, String type){
            this.text = text;
            this.type = type;
        }
        
        public String getData(int index){
            if(this.type.equals(MULTI_NUMERIC)){
                String[] data = this.text.split("\\|");
                if(index < data.length){
                    return data[index];
                }
                else{
                    return "out_of_bounds";
                }
            }
            else{
                return "not_allowed";
            }
        }

        public String getData(String index){
            if (this.type.equals(MULTI_ASSOC)) {
                int i = indexOf(index, keys);

                String[] data = this.text.split("\\|");
                if(i < data.length){
                    return data[i];
                }
                else{
                    return "out_of_bounds";
                }
            }
            else{
                return "not_allowed";
            }
        }
    }

    public class Series{
        public String name;
        public String response;
        public String[] data;
        public int myIndex;
        public String seriesText = null;
        public int rows = 0;
        public String[] inner_data;

        public Series(String name){
            this.name = name;
            this.response = str;
            data = str.split("\\`");
            String[] hh = data[0].split("\\*");
            series = hh[1].split("\\|");

            myIndex = indexOf(name, series);
            seriesText = data[myIndex + 1];
            inner_data = seriesText.split("\\*");
            this.rows = inner_data.length - 1;
            String[] jj = seriesText.split("\\*");
            keys = jj[0].split("\\|");
        }

        public Row getRow(int index){
            if (index < this.rows) {
                Row row = new Row(inner_data[index + 1], MULTI_ASSOC);
                return row;
            }
            else{
                return null;
            }
        }
    }
    
    public Response(String text){
        str = text;
        if(text.substring(0, 2).equals("..")){
            //everything okay
            str = str.substring(2);
            chars = str.split("\\*");
            type = chars[0];

            if (type.equals(ASSOC) || type.equals(MULTI_ASSOC)) {
                keys = chars[1].split("\\|");

                if (type.equals(MULTI_ASSOC)) {
                    rows = chars.length - 2;
                }
                else{
                    rows = chars.length - 1;
                }
            }

            if (type.equals(MULTI_SERIES)) {
                String[] in = chars[1].split("\\`");
                series = in[0].split("\\|");
            }
        }
        else{
            status = false;
            error = "Parse error.";
        }
    }
    
    public String get(int index){
        if(type.equals(NUMERIC)){
            int count = chars.length - 1;
            if(index < count){
                return chars[index+1];
            }
            else{
                return "out_of_bounds";
            }
        }
        else{
            return "not_allowed";
        }
    }
    
    public boolean contains(String[] stack, String needle){
        boolean status = false;
        for(int i = 0; i < stack.length; i++){
            if(stack[i].equals(needle)){
                status = true;
            }
        }
        return status;
    }
    
    public int indexOf(String needle, String[] stack){
        int res = 0-1;
        for(int i = 0; i < stack.length; i++){
            if(stack[i].equals(needle)){
                res = i;
            }
        }
        return res;
    }

    public Row getRow(int index){
        if (type.equals(MULTI_ASSOC)) {
            if((index+2) < chars.length){
                Row row = new Row(chars[index + 2], type);
                return row;
            }
            else{
                return null;
            }
        }
        else if (type.equals(MULTI_NUMERIC)) {
            if((index+1) < chars.length){
                Row row = new Row(chars[index + 1], type);
                return row;
            }
            else{
                return null;
            }
        }
        else{
            return null;
        }
    }
    
    public String get(String index){
        if(type.equals(ASSOC)){
            if(chars.length == 3){
                String[] keys = chars[1].split("\\|");
                String[] values = chars[2].split("\\|");
                if(contains(keys, index)){
                    return values[indexOf(index, keys)];
                }
                else{
                    return "out_of_bounds";
                }
            }
            else{
                return "parse_error";
            }
        }
        else{
            return "not_allowed";
        }
    }

    public Series getSeries(String name){
        int index = indexOf(name, series);

        if (index != (0-1)) {
            Series series = new Series(name);
            return series;
        }
        else{
            return null;
        }
    }
}