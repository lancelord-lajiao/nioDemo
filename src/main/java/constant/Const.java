package constant;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @ClassName Const
 * @Description //TODO
 * @Date 2019/5/16 19:46
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class Const

{
    @AllArgsConstructor
    public enum Order{
        QUERY_TIME_ORDER("1","2");
        String code;
        String value;
    }


}
