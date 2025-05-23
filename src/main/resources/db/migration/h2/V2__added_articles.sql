CREATE TABLE IF NOT EXISTS public.articles
(
    id SERIAL,
    date_of_creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    topic VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    CONSTRAINT articles_pkey PRIMARY KEY (id)
);
