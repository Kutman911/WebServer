import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
* Class for storing questions and answers
*/
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

  private static Map<String, Test> tests = new HashMap<>();

  static {
    // ---  A  ---
    List<Question> questionsA = new ArrayList<>();
    questionsA.add(new Question(
    "q1", "1. Продолжите числовую последовательность: 1, 4, 8, 13, 19, ?",
    Map.of("a", "25", "b", "26", "c", "27", "d", "28"), "b", false
    ));
    questionsA.add(new Question(
    "q2", "2. Выберите слово, которое дополнит аналогию: Хлеб : Пекарь :: Книга : ?",
    Map.of("a", "Читатель", "b", "Библиотека", "c", "Писатель", "d", "Бумага"), "c", false
    ));
    questionsA.add(new Question(
    "q3", "3. Какое слово лишнее в списке (по принципу биологического класса)?",
    Map.of("a", "Воробей", "b", "Слон", "c", "Кот", "d", "Собака"), "a", false
    ));
    questionsA.add(new Question(
    "q4", "4. Правда или Ложь: Слово \"карета\" может быть составлено только из букв, содержащихся в слове \"аптекарь\".",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsA.add(new Question(
    "q5", "5. Правда или Ложь: Слово \"кожа\" может быть составлено из первых букв слов в предложении: Каждый Охотник Желает Знать Где Сидит Фазан.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsA.add(new Question(
    "q6", "6. Правда или Ложь: У Гари 48 долларов. Если он займет 57 долларов у Джейн и 15 долларов у Джилл, он сможет купить велосипед за 120 долларов.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsA.add(new Question(
    "q7", "7. Правда или Ложь: Если человек смотрит в зеркало и дотрагивается до левого уха правой рукой, его отражение, кажется, дотрагивается до левого уха правой рукой.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsA.add(new Question(
    "q8", "8. Правда или Ложь: 27 минут до 7 часов - это 33 минуты после 5 часов.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsA.add(new Question(
    "q9", "9. Правда или Ложь: Если позавчера было воскресенье, то завтра будет среда.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsA.add(new Question(
    "q10", "10. Правда или Ложь: Если оставить буквы в том же порядке, но переставить пробелы во фразе \"Онаестна\", ее можно прочитать как \"Она ест на\".",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    tests.put("a", new Test("a", "IQ Тест А: Логика и Вербальные Навыки", questionsA));

    // --- B  ---
    List<Question> questionsB = new ArrayList<>();
    questionsB.add(new Question(
    "q1", "1. Продолжите числовую последовательность: 2, 6, 18, 54, ?",
    Map.of("a", "108", "b", "144", "c", "158", "d", "162"), "d", false
    ));
    questionsB.add(new Question(
    "q2", "2. Выберите слово, которое дополнит аналогию: Ночь : День :: Голод : ?",
    Map.of("a", "Сытость", "b", "Пища", "c", "Еда", "d", "Жажда"), "a", false
    ));
    questionsB.add(new Question(
    "q3", "3. Какое слово лишнее в списке (по принципу категории)?",
    Map.of("a", "Красный", "b", "Квадрат", "c", "Зеленый", "d", "Желтый"), "b", false
    ));
    questionsB.add(new Question(
    "q4", "4. Правда или Ложь: Если все кошки — животные, а некоторые животные — хищники, то все кошки — хищники.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsB.add(new Question(
    "q5", "5. Правда или Ложь: Внутри любого квадрата всегда можно нарисовать круг, касающийся всех четырех сторон.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsB.add(new Question(
    "q6", "6. Правда или Ложь: Если 4 кошки ловят 4 мыши за 4 минуты, то 1 кошка поймает 1 мышь за 1 минуту.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsB.add(new Question(
    "q7", "7. Правда или Ложь: Можно ли из слова \"советник\" составить слово \"ответ\"?",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsB.add(new Question(
    "q8", "8. Правда или Ложь: Поезд из А в Б идет 1 час 15 минут. Обратно он идет на 25 минут быстрее. Общее время в пути 2 часа 5 минут.",
    Map.of("true", "Правда", "false", "Ложь"), "true", true
    ));
    questionsB.add(new Question(
    "q9", "9. Правда или Ложь: Вода, Пар, Лед – это правильный порядок состояний вещества по мере уменьшения температуры.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    questionsB.add(new Question(
    "q10", "10. Правда или Ложь: В семье 5 сыновей, и у каждого есть сестра. Это значит, что в семье 10 детей.",
    Map.of("true", "Правда", "false", "Ложь"), "false", true
    ));
    tests.put("b", new Test("b", "IQ Тест B: Абстрактные Задачи и Смекалка", questionsB));
  }

  public static Test getTest(String id) {
    return tests.get(id);
  }
}
