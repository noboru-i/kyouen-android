Feature: 初期フロー
  Scenario: プレイしてみる
    Given テスト対象は "android" 端末
    Then "スタート" が表示されていること
    Then "test0" としてスクリーンショットを撮る
    When "スタート" をタップする
    Then "次へ" が表示されていること
    Then "test1" としてスクリーンショットを撮る
    When "次へ" をタップする
    Then "次へ" が表示されていること
    Then "test2" としてスクリーンショットを撮る
