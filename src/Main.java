import model.Student;
import util.FiledChecker;

public class Main {

    public static void main(String[] args) {
        FiledChecker filedChecker = new FiledChecker();
        Student student = new Student();
        student.setId(1);
        student.setBirthday("!");
        student.setName(null);
        student.setTelephone("15858293092");
        String errorInfo = filedChecker.checkAndGetErrorInfo(student);
        if (errorInfo == null) {
            System.out.println("完全正确");
        } else {
            System.out.println(errorInfo);
        }
    }
}
