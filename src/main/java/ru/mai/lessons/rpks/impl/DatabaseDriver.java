package ru.mai.lessons.rpks.impl;

import ch.qos.logback.core.joran.sanity.Pair;
import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.IDatabaseDriver;
import ru.mai.lessons.rpks.exception.FieldNotFoundInTableException;
import ru.mai.lessons.rpks.exception.WrongCommandFormatException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DatabaseDriver implements IDatabaseDriver {

    //group(1) = searchParameters, group(2) = tableName, group(3) = conditions, group(4) = parametersToGroup
    final Pattern requestWithWhereAndGroup = Pattern.compile("^SELECT=(\\S+) FROM=(\\S+)(?: WHERE=\\(([^= ]+='.+'(?: (?:OR|AND) [^= ]+='.+')*)\\))(?: GROUPBY=(.+))$");
    final Pattern requestWithWhere = Pattern.compile("^SELECT=(\\S+) FROM=(\\S+)(?: WHERE=\\(([^= ]+='.+'(?: (?:OR|AND) [^= ]+='.+')*)\\))$");
    final Pattern requestWithGroupBy = Pattern.compile("^SELECT=(\\S+) FROM=(\\S+)(?: GROUPBY=(\\S+))$");
    final Pattern request = Pattern.compile("^SELECT=(\\S+) FROM=(\\S+)$");

    enum typeRequest {
        REQUEST,
        REQUEST_WHERE,
        REQUEST_GROUPBY,
        REQUEST_WHEREGROUPBY;
    }

    final Pattern firstCondition = Pattern.compile("^([^= ]+)='([^']+)'(.*)?$");
    final Pattern nextCondition = Pattern.compile("^(AND|OR) ([^= ]+)='([^']+)'(.*)?$");

    final String prefix = "src/test/resources/";

    enum Union {
        AND,
        OR
    }

    String studentsFile;
    String groupsFile;
    String subjectsFile;
    String gradeFile;

    @Override
    public List<String> find(String studentsCsvFile, String groupsCsvFile, String subjectsCsvFile,
                             String gradeCsvFile, String command) throws FieldNotFoundInTableException, WrongCommandFormatException {
        List<String> result;
        List<String> requestDetails;

        studentsFile = prefix + studentsCsvFile;
        groupsFile = prefix + groupsCsvFile;
        subjectsFile = prefix + subjectsCsvFile;
        gradeFile = prefix + gradeCsvFile;

        DatabaseReader databaseReader = new DatabaseReader(studentsFile, groupsFile, subjectsFile, gradeFile);

        if ((requestDetails = requestParser(command, typeRequest.REQUEST_WHEREGROUPBY)).size() == 4 && noOneOfParametersIsEqualToZero(requestDetails)) {
            result = databaseReader.findProcess(requestDetails.get(0), requestDetails.get(1), requestDetails.get(2), requestDetails.get(3));
        } else if ((requestDetails = requestParser(command, typeRequest.REQUEST_WHERE)).size() == 3 && noOneOfParametersIsEqualToZero(requestDetails)) {
            result = databaseReader.findProcess(requestDetails.get(0), requestDetails.get(1), requestDetails.get(2), null);
        } else if ((requestDetails = requestParser(command, typeRequest.REQUEST_GROUPBY)).size() == 3 && noOneOfParametersIsEqualToZero(requestDetails)) {
            result = databaseReader.findProcess(requestDetails.get(0), requestDetails.get(1), null, requestDetails.get(2));
        } else if ((requestDetails = requestParser(command, typeRequest.REQUEST)).size() == 2 && noOneOfParametersIsEqualToZero(requestDetails)) {
            result = databaseReader.findProcess(requestDetails.get(0), requestDetails.get(1), null, null);
        } else {
            throw new WrongCommandFormatException("wrong command format");
        }

        return result;
    }

    private boolean noOneOfParametersIsEqualToZero(List<String> requestDetails) {
        for (String requestDetail : requestDetails) {
            if (requestDetail.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private List<String> requestParser(String line, typeRequest type) {
        Matcher matcher;
        switch (type) {
            case REQUEST:
                matcher = request.matcher(line);
                break;
            case REQUEST_WHERE:
                matcher = requestWithWhere.matcher(line);
                break;
            case REQUEST_GROUPBY:
                matcher = requestWithGroupBy.matcher(line);
                break;
            case REQUEST_WHEREGROUPBY:
                matcher = requestWithWhereAndGroup.matcher(line);
                break;
            default:
                return null;
        }

        List<String> list = new ArrayList<>();
        if (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                list.add(matcher.group(i));
            }
        }
        return list;
    }
}
