
CREATE TABLE IF NOT EXISTS Users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    elo_rating INT DEFAULT 1000,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0
    );

CREATE TABLE IF NOT EXISTS Pokedex (
                                       id INT PRIMARY KEY,
                                       name VARCHAR(50) NOT NULL,
    type1 VARCHAR(20) NOT NULL,
    type2 VARCHAR(20),
    base_hp INT NOT NULL,
    base_attack INT NOT NULL,
    base_defense INT NOT NULL,
    base_speed INT NOT NULL
    );

INSERT INTO Users (username, password_hash) VALUES ('Red', 'hash_password_aqui') ON CONFLICT DO NOTHING;
INSERT INTO Pokedex (id, name, type1, type2, base_hp, base_attack, base_defense, base_speed)
VALUES (25, 'Pikachu', 'Electric', NULL, 35, 55, 40, 90) ON CONFLICT DO NOTHING;