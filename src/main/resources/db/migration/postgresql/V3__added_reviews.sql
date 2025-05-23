CREATE TABLE IF NOT EXISTS public.reviews
(
    id SERIAL,
    type SMALLINT NOT NULL,
    date_of_creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content TEXT NOT NULL,
    article_id INTEGER NOT NULL,
    author_id INTEGER,
    CONSTRAINT reviwes_pkey PRIMARY KEY (id),
    CONSTRAINT users_fkey FOREIGN KEY (author_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON DELETE SET NULL,
    CONSTRAINT articles_fkey FOREIGN KEY (article_id)
        REFERENCES public.articles (id) MATCH SIMPLE
        ON DELETE CASCADE
);