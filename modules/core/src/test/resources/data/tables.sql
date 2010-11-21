CREATE
    TABLE FAQ
    (
        FAQID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
        ANSWER VARCHAR(1024) NOT NULL,
        CREATIONTIMESTAMP TIMESTAMP NOT NULL,
        CREATOR VARCHAR(32) NOT NULL,
        MODIFIEDTIMESTAMP TIMESTAMP NOT NULL,
        MODIFIER VARCHAR(32) NOT NULL,
        PUBLISHED SMALLINT NOT NULL,
        QUESTION VARCHAR(1024) NOT NULL,
        PRIMARY KEY (FAQID)
    );

CREATE
    TABLE ATTACHMENT
    (
        ATTACHMENTID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
        NAME VARCHAR(64) NOT NULL,
        ATTACHMENT BLOB(100000) NOT NULL,
        LENGTH INTEGER NOT NULL,
        FAQID BIGINT NOT NULL,
        PRIMARY KEY (ATTACHMENTID),
        CONSTRAINT FK1C935436F4BAC21 FOREIGN KEY (FAQID) REFERENCES FAQ (FAQID)
    );

CREATE INDEX IDX1C935436F4BAC21 ON ATTACHMENT(FAQID);