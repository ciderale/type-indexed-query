CREATE TABLE "user" (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL,
    active BOOLEAN NOT NULL
);

INSERT INTO "user" (id, name, email, active) VALUES
('1', 'Alice', 'alice@example.com', true),
('2', 'Bob', 'bob@example.com', true),
('3', 'Charlie', 'charlie@example.com', false);