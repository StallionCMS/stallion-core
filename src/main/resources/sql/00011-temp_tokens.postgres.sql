CREATE TABLE stallion_temp_tokens (
    id bigint NOT NULL PRIMARY KEY,
    customkey character varying(100) UNIQUE NOT NULL,
    expiresat timestamp without time zone,
    token character varying(75) NOT NULL,
    usertype character varying(75) NOT NULL,
    targetkey character varying(75) NOT NULL,
    createdat timestamp without time zone,
    usedat timestamp without time zone,
    data text DEFAULT ''::text
);

