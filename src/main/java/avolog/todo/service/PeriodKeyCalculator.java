package avolog.todo.service;

import avolog.todo.domain.PeriodType;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import org.springframework.stereotype.Component;

@Component
public class PeriodKeyCalculator {

    public String calculate(PeriodType period, LocalDate date, Integer weekStartDay, Integer monthBaseDay) {
        return switch (period) {
            case DAILY -> date.toString();
            case WEEKLY -> weeklyKey(date, weekStartDay);
            case MONTHLY -> monthlyKey(date, monthBaseDay);
        };
    }

    private String weeklyKey(LocalDate date, Integer weekStartDay) {
        int startDay = weekStartDay == null ? 1 : weekStartDay;
        WeekFields wf = WeekFields.of(startDay, 4);
        int week = date.get(wf.weekOfWeekBasedYear());
        int year = date.get(wf.weekBasedYear());
        return String.format("%d-W%02d", year, week);
    }

    private String monthlyKey(LocalDate date, Integer monthBaseDay) {
        int day = monthBaseDay == null ? 1 : monthBaseDay;
        LocalDate anchor = date.getDayOfMonth() >= day
                ? LocalDate.of(date.getYear(), date.getMonth(), day)
                : LocalDate.of(date.minusMonths(1).getYear(), date.minusMonths(1).getMonth(), day);
        YearMonth ym = YearMonth.of(anchor.getYear(), anchor.getMonth());
        return String.format("%d-%02d", ym.getYear(), ym.getMonthValue());
    }
}
