CREATE TABLE IF NOT EXISTS public.reviews
(
    id SERIAL,
    type SMALLINT NOT NULL,
    date_of_creation TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    content TEXT NOT NULL,
    article_id INTEGER NOT NULL,
    author_id INTEGER,
    CONSTRAINT reviwes_pkey PRIMARY KEY (id),
    CONSTRAINT reviews_users_fkey FOREIGN KEY (author_id)
        REFERENCES public.users (id)
        ON DELETE SET NULL,
    CONSTRAINT reviews_articles_fkey FOREIGN KEY (article_id)
        REFERENCES public.articles (id)
        ON DELETE CASCADE
);