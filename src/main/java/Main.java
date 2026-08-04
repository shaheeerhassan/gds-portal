import com.school.model.User;
import com.school.utils.PasswordEncryption;
import com.school.utils.TokenGenerator;
import com.school.utils.UsernameGenerator;
import com.school.validations.ValidatorUtil;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        String phone = "923213104282";
        System.out.println(ValidatorUtil.validatePhone(phone));
    }
}
