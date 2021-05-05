package wooteco.subway.exception;

public class DuplicatedNameException extends SubwayException {

    private static final String DUPLICATED_NAME_ERROR_MESSAGE = "중복된 이름입니다.";

    public DuplicatedNameException() {
        super(DUPLICATED_NAME_ERROR_MESSAGE);
    }
}
