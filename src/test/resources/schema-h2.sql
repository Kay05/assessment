-- H2 Database Schema for Tests
CREATE TABLE IF NOT EXISTS members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    birthday DATE NOT NULL,
    current_rank INTEGER NOT NULL,
    games_played INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS matches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player1_id BIGINT NOT NULL,
    player2_id BIGINT NOT NULL,
    result VARCHAR(50) NOT NULL,
    player1_rank_before INTEGER NOT NULL,
    player2_rank_before INTEGER NOT NULL,
    player1_rank_after INTEGER NOT NULL,
    player2_rank_after INTEGER NOT NULL,
    match_date TIMESTAMP NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (player1_id) REFERENCES members(id),
    FOREIGN KEY (player2_id) REFERENCES members(id)
);