package OOLL_P_Student;

public class Lecture {
	private String name;
    private int startHour, startMinute;
    private int endHour, endMinute;

    public Lecture(String name, int sh, int sm, int eh, int em) {
        this.name = name;
        this.startHour = sh;
        this.startMinute = sm;
        this.endHour = eh;
        this.endMinute = em;
    }

    public String getName() { return name; }
    public int getStartHour() { return startHour; }
    public int getStartMinute() { return startMinute; }
    public int getEndHour() { return endHour; }
    public int getEndMinute() { return endMinute; }
}