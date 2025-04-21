CREATE TABLE IF NOT EXISTS public.authorship_of_articles
(
    author_id INTEGER NOT NULL,
    article_id INTEGER NOT NULL,
    CONSTRAINT users_fkey FOREIGN KEY (author_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE,
    CONSTRAINT articles_fkey FOREIGN KEY (article_id)
        REFERENCES public.articles (id) MATCH SIMPLE
        ON UPDATE RESTRICT
        ON DELETE CASCADE
);