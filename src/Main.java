import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;

import java.io.*;
import java.sql.*;
import java.util.Objects;

public class Main {
    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1521/xepdb1";
    private static final String DB_USERNAME = "sajad";
    private static final String DB_PASSWORD = "myjava123";

    public static void main(String[] args) {
//        prepareDatabase();
//        createArffFileFromDatabase("PossumTrain");
//        createArffFileFromDatabase("PossumQuery");
        predictPossumGender();
//        initialPossumResultDatabaseTable();
    }

    private static void initialPossumResultDatabaseTable() {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = connection.getMetaData().getTables(null, null, "POSSUMRESULT", new String[] {"TABLE"});
            if (!resultSet.next()) {
                statement.execute("CREATE TABLE PossumResult (case NUMBER, site NUMBER, Pop VARCHAR2(50), age NUMBER, hdlngth NUMBER, skullw NUMBER, totlngth NUMBER, taill NUMBER, footlgth NUMBER, earconch NUMBER, eye NUMBER, chest NUMBER, belly NUMBER, sex VARCHAR2(1))");
            }
            populatePossumTable("src/assets/PossumResult.arff", "PossumResult", statement);

            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    private static void predictPossumGender() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader("src/assets/PossumTrain.arff"));
            Instances possumTrain = new Instances(bufferedReader);
            possumTrain.setClassIndex(possumTrain.numAttributes() - 1);
            bufferedReader = new BufferedReader(new FileReader("src/assets/PossumQuery.arff"));
            Instances possumQuery = new Instances(bufferedReader);
            possumQuery.setClassIndex(possumQuery.numAttributes() - 1);

            bufferedReader.close();

            J48 tree = new J48();
            tree.buildClassifier(possumTrain);

            evaluateTreeSummary(possumTrain, tree);

            Instances labeled = new Instances(possumQuery);
            for (int i = 0; i < possumQuery.numInstances(); i++) {
                double clsLabel = tree.classifyInstance(possumQuery.instance(i));
                labeled.instance(i).setClassValue(clsLabel);
            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("src/assets/PossumResult.arff"));
            bufferedWriter.write(labeled.toString());
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void evaluateTreeSummary(Instances possumTrain, J48 tree) throws Exception {
        Evaluation eval = new Evaluation(possumTrain);
        eval.evaluateModel(tree, possumTrain);
//        System.out.println(eval.toSummaryString());
        System.out.println("______________________________________________");
        System.out.println("eval.precision() = " + eval.precision(0) * 100);
        System.out.println("eval.recall() = " + eval.recall(0) * 100);
        System.out.println("eval.fMeasure() = " + eval.fMeasure(0) * 100);
        System.out.println("______________________________________________");
//        System.out.println(tree.graph());
//        System.out.println(tree);
    }

    private static void createArffFileFromDatabase(String tableName) {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);

            FastVector attributes = new FastVector();
            attributes.addElement(new Attribute("case"));
            attributes.addElement(new Attribute("site"));
            FastVector popValues = new FastVector();
            popValues.addElement("Vic");
            popValues.addElement("other");
            attributes.addElement(new Attribute("Pop", popValues));
            attributes.addElement(new Attribute("age"));
            attributes.addElement(new Attribute("hdlngth"));
            attributes.addElement(new Attribute("skullw"));
            attributes.addElement(new Attribute("totlngth"));
            attributes.addElement(new Attribute("taill"));
            attributes.addElement(new Attribute("footlgth"));
            attributes.addElement(new Attribute("earconch"));
            attributes.addElement(new Attribute("eye"));
            attributes.addElement(new Attribute("chest"));
            attributes.addElement(new Attribute("belly"));
            FastVector sexValues = new FastVector();
            sexValues.addElement("m");
            sexValues.addElement("f");
            attributes.addElement(new Attribute("sex", sexValues));

            Instances data = new Instances("PossumTrain", attributes, 0);
            data.setClassIndex(data.numAttributes() - 1);
            while (resultSet.next()) {
                Instance inst = new Instance(attributes.size());
                for (int i = 0; i < attributes.size(); i++) {
                    if (resultSet.getObject(i + 1) != null) {
                        if (attributes.elementAt(i) instanceof Attribute attr) {
                            if (attr.isNumeric()) {
                                inst.setValue(attr, resultSet.getDouble(i + 1));
                            } else if (!Objects.equals(resultSet.getString(i + 1), "?")) {
                                inst.setValue(attr, resultSet.getString(i + 1));
                            }
                        }
                    }
                }
                data.add(inst);
            }

            ArffSaver saver = new ArffSaver();
            saver.setInstances(data);
            saver.setFile(new File("src/assets/" + tableName + ".arff"));
            saver.writeBatch();

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }

    private static void prepareDatabase() {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = connection.getMetaData().getTables(null, null, "POSSUMTRAIN", new String[] {"TABLE"});
            if (!resultSet.next()) {
                statement.execute("CREATE TABLE PossumTrain (case NUMBER, site NUMBER, Pop VARCHAR2(50), age NUMBER, hdlngth NUMBER, skullw NUMBER, totlngth NUMBER, taill NUMBER, footlgth NUMBER, earconch NUMBER, eye NUMBER, chest NUMBER, belly NUMBER, sex VARCHAR2(1))");
            }
            resultSet = connection.getMetaData().getTables(null, null, "POSSUMQUERY", new String[] {"TABLE"});
            if (!resultSet.next()) {
                statement.execute("CREATE TABLE PossumQuery (case NUMBER, site NUMBER, Pop VARCHAR2(50), age NUMBER, hdlngth NUMBER, skullw NUMBER, totlngth NUMBER, taill NUMBER, footlgth NUMBER, earconch NUMBER, eye NUMBER, chest NUMBER, belly NUMBER, sex VARCHAR2(1))");
            }
            populatePossumTable("src/assets/DatasetTrain.csv", "PossumTrain", statement);
            populatePossumTable("src/assets/DatasetQuery.csv", "PossumQuery", statement);

            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populatePossumTable(String filePath, String tableName, Statement statement) throws IOException, SQLException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        String[] split = filePath.split("\\.");
        boolean isArffFormat = Objects.equals(split[1], "arff");
        if (isArffFormat) {
            boolean foundDataSection = false;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("@data")) {
                    foundDataSection = true;
                    continue;
                }
                if (foundDataSection) {
                    insertDataToTable(tableName, statement, line);
                }
            }

        } else {
            boolean skipHeader = true;
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                insertDataToTable(tableName, statement, line);
            }
        }

        reader.close();
    }

    private static void insertDataToTable(String tableName, Statement statement, String line) throws SQLException {
        String[] data = line.split(",");
        data[2] = "'" + data[2] + "'";
        data[13] = "'" + data[13] + "'";
        String insertQuery = String.format("INSERT INTO " + tableName + "(case, site, Pop, age, hdlngth, skullw, totlngth, taill, footlgth, earconch, eye, chest, belly, sex) VALUES (%s)", String.join(",", data));
        statement.executeUpdate(insertQuery);
    }

    private static void dropPossumTables() {
        try {
            Connection connection = getConnection();
            Statement statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE PossumTrain");
            statement.executeUpdate("DROP TABLE PossumQuery");
            statement.executeUpdate("DROP TABLE PossumResult");
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}