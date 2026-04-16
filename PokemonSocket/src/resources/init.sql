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

CREATE TABLE IF NOT EXISTS Moves (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL,
    power INT,
    accuracy INT,
    pp INT NOT NULL,
    category VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS TrainerPokemon (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES Users(id) ON DELETE CASCADE,
    pokedex_id INT NOT NULL REFERENCES Pokedex(id),
    nickname VARCHAR(50),
    level INT DEFAULT 5,
    iv_hp INT DEFAULT 31,
    iv_attack INT DEFAULT 31,
    iv_defense INT DEFAULT 31,
    iv_speed INT DEFAULT 31,
    ev_hp INT DEFAULT 0,
    ev_attack INT DEFAULT 0,
    ev_defense INT DEFAULT 0,
    ev_speed INT DEFAULT 0,
    current_hp INT,
    status VARCHAR(20) DEFAULT 'NONE',
    move1_id INT REFERENCES Moves(id),
    move2_id INT REFERENCES Moves(id),
    move3_id INT REFERENCES Moves(id),
    move4_id INT REFERENCES Moves(id),
    position INT CHECK (position BETWEEN 1 AND 6)
);

INSERT INTO Users (username, password_hash) VALUES ('Red', 'hash_password_aqui') ON CONFLICT DO NOTHING;
INSERT INTO Pokedex (id, name, type1, type2, base_hp, base_attack, base_defense, base_speed)
VALUES (25, 'Pikachu', 'Electric', NULL, 35, 55, 40, 90) ON CONFLICT DO NOTHING;

INSERT INTO Moves (name, type, power, accuracy, pp, category) VALUES
('Thunder Shock', 'Electric', 40, 100, 30, 'SPECIAL'),
('Quick Attack', 'Normal', 40, 100, 30, 'PHYSICAL') ON CONFLICT DO NOTHING;

INSERT INTO TrainerPokemon (user_id, pokedex_id, nickname, level, iv_hp, iv_attack, iv_defense, iv_speed,
                            ev_hp, ev_attack, ev_defense, ev_speed, current_hp, status, move1_id, move2_id, move3_id, move4_id, position)
VALUES
((SELECT id FROM Users WHERE username = 'Red'), 25, 'Sparky', 5, 31, 31, 31, 31, 0, 0, 0, 0, 35, 'NONE', 1, 2, NULL, NULL, 1)
ON CONFLICT DO NOTHING;
