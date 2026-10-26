DECLARE
    PROCEDURE adicionar_coluna(
        nome_coluna VARCHAR2,
        definicao_coluna VARCHAR2
    ) IS
        quantidade NUMBER;
    BEGIN
        SELECT COUNT(*)
        INTO quantidade
        FROM USER_TAB_COLUMNS
        WHERE TABLE_NAME = 'T_ETP_PREVISAO_DEMANDA'
          AND COLUMN_NAME = UPPER(nome_coluna);

        IF quantidade = 0 THEN
            EXECUTE IMMEDIATE
                'ALTER TABLE T_ETP_PREVISAO_DEMANDA ADD ('
                || nome_coluna
                || ' '
                || definicao_coluna
                || ')';
        END IF;
    END;
BEGIN
    adicionar_coluna(
        'NR_COEF_CORRELACAO',
        'NUMBER(12, 6)'
    );

    adicionar_coluna(
        'NR_COEF_DETERMINACAO',
        'NUMBER(12, 6)'
    );

    adicionar_coluna(
        'NR_COEF_ANGULAR',
        'NUMBER(16, 6)'
    );

    adicionar_coluna(
        'NR_INTERCEPTO',
        'NUMBER(16, 6)'
    );

    adicionar_coluna(
        'DS_EQUACAO',
        'VARCHAR2(150)'
    );

    adicionar_coluna(
        'DS_FORCA_CORRELACAO',
        'VARCHAR2(20)'
    );

    adicionar_coluna(
        'DS_TENDENCIA_REGRESSAO',
        'VARCHAR2(20)'
    );

    adicionar_coluna(
        'NR_QTD_DIAS_REGRESSAO',
        'NUMBER(4)'
    );

    adicionar_coluna(
        'NR_VARIACAO_ESTIMADA',
        'NUMBER(10, 2)'
    );

    adicionar_coluna(
        'DS_INTERPRETACAO_REGRESSAO',
        'VARCHAR2(1000)'
    );
END;
/

SELECT
    COLUMN_NAME,
    DATA_TYPE,
    DATA_PRECISION,
    DATA_SCALE,
    DATA_LENGTH,
    NULLABLE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'T_ETP_PREVISAO_DEMANDA'
  AND COLUMN_NAME IN (
      'NR_COEF_CORRELACAO',
      'NR_COEF_DETERMINACAO',
      'NR_COEF_ANGULAR',
      'NR_INTERCEPTO',
      'DS_EQUACAO',
      'DS_FORCA_CORRELACAO',
      'DS_TENDENCIA_REGRESSAO',
      'NR_QTD_DIAS_REGRESSAO',
      'NR_VARIACAO_ESTIMADA',
      'DS_INTERPRETACAO_REGRESSAO'
  )
ORDER BY COLUMN_NAME;