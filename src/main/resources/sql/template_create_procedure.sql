USE [nature]
GO

/****** Object:  StoredProcedure [dbo].[pc_remove_duplication]    Script Date: 05/27/2019 14:37:48 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

IF EXISTS(SELECT * FROM sysobjects WHERE TYPE='P' AND name = 'pc_remove_duplication')
    BEGIN
      PRINT 'PROCEDURE: pc_remove_duplication has been existed already'
      DROP procedure dbo.pc_remove_duplication
    END
GO

CREATE PROCEDURE [dbo].[pc_remove_duplication]
( @jabt varchar(50) , @issn8 char(8), @volume char(12),
	@issue char(10), @atl varchar(920),
  @ppf char(5), @ppl char(5)
)
AS
BEGIN
	DECLARE @tb_tmp_art TABLE(artid INT)
	DECLARE	@artid INT
	DECLARE @tb_tmp_aut TABLE(aid INT)
	DECLARE @aid INT
	DECLARE @tb_tmp_aff TABLE(affid INT)
	DECLARE @affid INT
	SELECT artid INTO #tb_tmp_art
	FROM nature.dbo.content
	WHERE 1=1 AND ((@jabt IS NULL) OR (jabt = @jabt)) AND ((@jabt IS NOT NULL) OR (jabt IS NULL))
            AND ((@issn8 IS NULL) OR (issn8 = @issn8)) AND ((@issn8 IS NOT NULL) OR (issn8 IS NULL))
            AND ((@volume IS NULL) OR (vid = @volume)) AND ((@volume IS NOT NULL) OR (vid IS NULL))
            AND ((@issue IS NULL) OR (iid = @issue)) AND ((@issue IS NOT NULL) OR (iid IS NULL))
            AND ((@atl IS NULL) OR (atl = @atl)) AND ((@atl IS NOT NULL) OR (atl IS NULL))
            AND ((@ppf IS NULL) OR (ppf = @ppf)) AND ((@ppf IS NOT NULL) OR (ppf IS NULL))
            AND ((@ppl IS NULL) OR (ppl = @ppl)) AND ((@ppl IS NOT NULL) OR (ppl IS NULL))
	SELECT @artid = MIN(artid) FROM #tb_tmp_art
	WHILE @artid IS NOT NULL
		BEGIN
			SELECT aid INTO #tb_tmp_aut FROM nature.dbo.author WHERE artid = @artid
			SELECT affid INTO #tb_tmp_aff FROM nature.dbo.aff WHERE artid = @artid
			-- SELECT @aid = MIN(aid) FROM #tb_tmp_aut
			-- BEGIN TRY
        SET   TRANSACTION   ISOLATION   LEVEL   REPEATABLE READ
        BEGIN TRAN T1
          DELETE FROM nature.dbo.author_aff
          WHERE aid IN (SELECT aid FROM #tb_tmp_aut)
                OR affid IN (SELECT affid FROM #tb_tmp_aff)
        COMMIT TRAN T1
				SET   TRANSACTION   ISOLATION   LEVEL   REPEATABLE READ
				BEGIN TRAN T2
					DELETE FROM nature.dbo.aff WHERE artid = @artid
					DELETE FROM nature.dbo.author WHERE artid = @artid
					DELETE FROM nature.dbo.content WHERE artid = @artid
					DELETE FROM nature.dbo.tb_article WHERE artid = @artid
				COMMIT TRAN T2
			-- END TRY
			-- BEGIN CATCH
			IF @@ERROR <> 0
				IF @@TRANCOUNT > 0
					BEGIN
						ROLLBACK TRAN
					END
				ELSE
					BEGIN
						PRINT 'SUCCESS TO CASCADING DELETE'
					END
			-- END CATCH
			DROP TABLE #tb_tmp_aut
			DROP TABLE #tb_tmp_aff
			SELECT @artid = MIN(artid) FROM #tb_tmp_art WHERE artid > @artid
    END
	RETURN
	DROP TABLE #tb_tmp_art
END
GO