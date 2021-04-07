package Server;

import content.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Класс, обеспечивающий доступ к коллекции
 */

@XmlType(name = "root")
@XmlRootElement
public class CollectionManager {
    @XmlElementWrapper(name = "collection")
    private final LinkedList<Flat> flats = new LinkedList<>();
    private final static LocalDateTime initDate = LocalDateTime.now();
    private Handler handler;
    private static final Set<String> pathList = new HashSet<>();
    Connector connector;

    public CollectionManager() {
    }

    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public static LocalDateTime getInitDate() {
        return initDate;
    }

    /**
     * Получить информацию о командах
     */
    public void help() {
        String res = "help: Вывести справку по доступным командам\n" +
                      "info: Вывести в стандартный поток вывода информацию о коллекции (тип, дата инициализации, количество элементов и т.д.)\n" +
                      "show: Вывести в стандартный поток вывода все элементы коллекции в строковом представлении\n" +
                      "add {element}: Добавить новый элемент в коллекцию\n" +
                      "update id {element}: Обновить значение элемента коллекции, id которого равен заданному\n" +
                      "remove_by_id id: Удалить элемент коллекции по его id\n" +
                      "clear: Очистить коллекцию\n" +
//                      "save: Сохранить коллекцию в файл\n" +
                      "execute_script file_name: Считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.\n" +
                      "exit: Завершить программу (без сохранения в файл)\n" +
                      "remove_at index: Удалить элемент, находящийся в заданной позиции коллекции (index)\n" +
                      "remove_last: Удалить последний элемент из коллекции\n" +
                      "shuffle: Перемешать элементы коллекции в случайном порядке\n" +
                      "average_of_living_space: Вывести среднее значение поля livingSpace для всех элементов коллекции\n" +
                      "max_by_house: Вывести любой объект из коллекции, значение поля house которого является максимальным\n" +
                      "filter_less_than_view view: Вывести элементы, значение поля view которых меньше заданного";
        connector.send(res);
    }

    /**
     * Получить информацию о коллекции
     */
    public void info() {
        String info = "Тип - " + flats.getClass().getName() +
                "\nДата инициализации - " + getInitDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss")) +
                "\nКоличество элементов - " + flats.size();
        connector.send(info);
    }

    /**
     * Показать элементы коллекции
     */
    public void show() {
        flats.forEach(flat -> connector.send(flat.NiceToString()));
    }

    /**
     * Добавить элемент коллекции
     */
    public void add(){
        connector.send("===================================\nЭлемент успешно добавлен.");
    }

    /**
     * Обновить существующий элемент коллекции по его id
     * @param id : id (не индекс) нужного элемента
     */
    public void update(String id, Flat argument) {
        for (Flat flat : flats) {
            if (flat.getId() == Long.parseLong(id)) {
                flats.add(argument);
                flats.getLast().setId(flat.getId());
                flats.set(flats.indexOf(flat), flats.getLast());
                remove_last();
                connector.send("===================================\nЭлемент успешно обновлён.");
                return;
            }
        }
        connector.send("Элемент с указанным id не найден.");
    }


    public void exit(){
        handler.isExit = true;
    }
    /**
     * Удалить элемент коллекции по его id
     *
     * @param id id(не индекс) нужного элемента
     */
    public void remove_by_id(String id) {
        if (flats.stream().anyMatch(flat -> flat.getId() == Long.parseLong(id))) {
            flats.remove(flats.stream().filter(flat -> flat.getId() == Long.parseLong(id)).findFirst().orElse(null));
            connector.send("Элемент успешно удалён.");
        } else connector.send("Элемента с id = '" + id + "' не найдено.");
    }

    /**
     * Очистить коллекцию
     */
    public void clear() {
        flats.clear();
        connector.send("Коллекция успешно очищена.");
    }

    /**
     * Сохранить коллекцию в файл
     * @param manager : объект для доступа к коллекции
     */
    public void save(CollectionManager manager){
        try {
            FileWriter writer = new FileWriter("C:\\Users\\User\\IdeaProjects\\lab5_maven\\src\\main\\java\\inputData\\output.xml");
            JAXBContext context = JAXBContext.newInstance(Flat.class, CollectionManager.class, House.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(manager, writer);
            connector.send("Коллекция успешно сохранена.");
        }catch (FileNotFoundException e) {
            connector.send("Ошибка. Файл для сохранения не найден, проверьте путь и доступ к файлу.");
        } catch (IOException e) {
            connector.send("Ошибка сохранения.");
        } catch (MarshalException e) {
            connector.send("Ошибка сериализации коллекции в XML.");
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    /**
     * Удалить элемент коллекции по его индексу
     * @param index : индекс нужного элемента
     */
    public void remove_at(String index) {
            try {
                flats.remove(Integer.parseInt(index));
                connector.send("Элемент коллекции успешно удалён.");
            } catch (IndexOutOfBoundsException ex) {
                connector.send("Ошибка. Элемента с таким индексом не существует.");
            }
        }

    /**
     * Удалить последний элемент коллекции
     */
    public void remove_last() {
        try {
            flats.removeLast();
        } catch (NoSuchElementException ex) {
            connector.send("Ошибка. Невозможно удалить последний элемент коллекции, т.к. она пуста.");
        }

    }

    /**
     * Перемешать элементы коллекции
     */
    public void shuffle() {
        Collections.shuffle(flats);
        connector.send("Элементы коллекции успешно перемешаны.");
    }

    /**
     * Вывести среднее значение поля livingSpace для всех элементов коллекции
     */
    public void average_of_living_space() {
        if (flats.size() > 0) {
            connector.send("Cреднее значение поля livingSpace равно " + flats.stream().mapToLong(Flat::getLivingSpace).average());
        } else connector.send("Ошибка. Коллекция пуста.");
    }

    /**
     * Вывести элемент коллекции с максимальным значением годом постройки дома
     */
    public void max_by_house() {
        if (flats.stream().findAny().isPresent())
            connector.send(flats.stream().max(Comparator.comparing(Flat::getHouse)).get().NiceToString());
        else connector.send("Ошибка. Коллекция пуста.");
    }

    /**
     * Вывести элементы коллекции, у которых значение поля View меньше заданного
     * @param view : объект Enum'a View, относительно которого нужно фильтровать
     */
    public void filter_less_than_view(String view) {
        if (flats.stream().anyMatch(flat -> flat.getView().compareTo(View.valueOf(view)) < 0))
            flats.stream().filter(flat -> flat.getView().compareTo(View.valueOf(view)) < 0).forEach(flat -> connector.send(flat.NiceToString()));
        else connector.send("Не найдено элементов со значением поля view меньше заданного.");
    }

    /**
     * Выполнить скрипт из заданного файла
     * @param path Путь до файла
     * @throws FileNotFoundException если файл недоступен/не найден
     */
    public void execute_script(String path) throws FileNotFoundException {
        InputStream stream;
        if (path.substring(0,4).equals("/dev")){
            connector.send("Файл для извлечения скрипта не найден. Проверьте путь и права доступа к файлу.");
            return;
        }
        try{
            stream = new BufferedInputStream(new FileInputStream(path));
        }catch (FileNotFoundException e){
            connector.send("Файл для извлечения скрипта не найден. Проверьте путь и права доступа к файлу.");
            return;
        }
        if (handler.getStream().equals(System.in)) {
            pathList.clear();
        } else {
            if (pathList.contains(path)) {
                connector.send("#############################################\nОшибка! Один или несколько скриптов зациклены.\n#############################################");
                return;
            }
        }
        //Проверка на зацикленность
        pathList.add(path);
        connector.send("====  Начало выполнения скрипта по адресу " + path + "  ====");
        handler.interactiveMod(stream);
        connector.send("====  Скрипт " + path + " успешно выполнен  ====\n");
        pathList.remove(path);
    }

    /**
     * Получить объект коллекции
     * @return коллекцию
     */
    public LinkedList<Flat> getFlats() {
        return flats;
    }

}

