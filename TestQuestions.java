import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestQuestions {

  public static class Question {
    public String id;
    public String text;
    public Map<String, String> options;
    public String correctAnswerKey;
    public boolean isTrueFalse;

    public Question(String id, String text, Map<String, String> options, String correctKey, boolean isTrueFalse) {
      this.id = id;
      this.text = text;
      this.options = options;
      this.correctAnswerKey = correctKey;
      this.isTrueFalse = isTrueFalse;
    }
  }

  public static class Test {
    public String id;
    public String title;
    public List<Question> questions;

    public Test(String id, String title, List<Question> questions) {
      this.id = id;
      this.title = title;
      this.questions = questions;
    }
  }

  private static final Map<String, Test> tests = new HashMap<>();

  static {
    // ====================== ТЕСТ A ======================
    List<Question> a = new ArrayList<>();
    a.add(new Question("q1", "1. Продолжите ряд: 2, 5, 10, 17, 26, ?",
    Map.of("a", "36", "b", "37", "c", "38", "d", "39"), "b", false));
    a.add(new Question("q2", "2. Птица : Небо :: Рыба : ?",
    Map.of("a", "Вода", "b", "Река", "c", "Аквариум", "d", "Плавники"), "a", false));
    a.add(new Question("q3", "3. Какое слово лишнее?",
    Map.of("a", "Яблоко", "b", "Банан", "c", "Морковь", "d", "Груша"), "c", false));
    a.add(new Question("q4", "4. Правда или Ложь: В году 12 месяцев, а в неделе 7 дней. Значит, в месяце примерно 4 недели.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true));
    a.add(new Question("q5", "5. У отца 5 дочерей. У каждой дочери есть брат. Сколько всего детей?",
    Map.of("a", "10", "b", "6", "c", "5", "d", "11"), "b", false));
    a.add(new Question("q6", "6. Какое число следующее: 1, 4, 9, 16, 25, ?",
    Map.of("a", "36", "b", "49", "c", "64", "d", "81"), "a", false));
    a.add(new Question("q7", "7. Правда или Ложь: Если все люди смертны, а Сократ — человек, то Сократ бессмертен.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true));
    a.add(new Question("q8", "8. Что тяжелее: килограмм ваты или килограмм железа?",
    Map.of("a", "Железо", "b", "Вата", "c", "Одинаково", "d", "Зависит от объёма"), "c", false));
    a.add(new Question("q9", "9. Правда или Ложь: Слово «стол» можно переставить в «слот».",
    Map.of("true", "Правда", "false", "Ложь"), "true", true));
    a.add(new Question("q10", "10. Сколько животных взял Моисей в ковчег?",
    Map.of("a", "Много", "b", "2 от каждого вида", "c", "Нисколько", "d", "7 пар чистых"), "c", false));
    tests.put("a", new Test("a", "IQ Тест A: Классика и Ловушки", a));

    // ====================== ТЕСТ B: ======================
    List<Question> b = new ArrayList<>();
    b.add(new Question("q1", "1. Продолжите ряд: 1, 3, 6, 10, 15, ?",
    Map.of("a", "20", "b", "21", "c", "22", "d", "25"), "b", false));
    b.add(new Question("q2", "2. Книга : Библиотека :: Машина : ?",
    Map.of("a", "Дорога", "b", "Гараж", "c", "Колесо", "d", "Бензин"), "b", false));
    b.add(new Question("q3", "3. Какое число лишнее: 3, 7, 11, 15, 19?",
    Map.of("a", "3", "b", "7", "c", "11", "d", "15"), "d", false));
    b.add(new Question("q4", "4. Правда или Ложь: Если 5 кошек ловят 5 мышей за 5 минут, то 100 кошек поймают 100 мышей за 5 минут.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true));
    b.add(new Question("q5", "5. Правда или Ложь: Бэтмен — это Брюс Уэйн.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true));
    b.add(new Question("q6", "6. Какое слово читается одинаково слева направо и справа налево?",
    Map.of("a", "Топот", "b", "Ротор", "c", "Казак", "d", "Все вышеперечисленные"), "d", false));
    b.add(new Question("q7", "7. Правда или Ложь: В сутках 24 часа, а в году 365 дней. Значит, в году 8760 часов.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true));
    b.add(new Question("q8", "8. Если вы обогнали бегуна на втором месте, на каком вы теперь?",
    Map.of("a", "На первом", "b", "На втором", "c", "На третьем", "d", "Неизвестно"), "b", false));
    b.add(new Question("q9", "9. Правда или Ложь: Можно составить слово «дом» из букв слова «мёд».",
    Map.of("true", "Правда", "false", "Ложь"), "false", true));
    b.add(new Question("q10", "10. Врач сказал: «У вас отличное зрение!» — «Спасибо, доктор!» — «А теперь прочитайте нижнюю строку». Что прочитал пациент?",
    Map.of("a", "Свою фамилию", "b", "Нижнюю строку таблицы", "c", "Ничего — он слепой", "d", "«Сделано в Китае»"), "d", false));
    tests.put("b", new Test("b", "IQ Тест B: Смекалка и Внимание", b));
  }

  public static Test getTest(String id) {
    Test test = tests.get(id);
    return test != null ? test : tests.get("a"); 
  }
}
