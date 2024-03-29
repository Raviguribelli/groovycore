package groovy

/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

/**
 * Tests for ClosureComparator
 *
 * @author Alexey Verkhovsky
 * @version $Revision$
 */
 class ClosureComparatorTest extends GroovyTestCase {


  public void testClosureComparatorForGroovyObjects() {

    def comparator = new ClosureComparator() { one, another ->
      one.greaterThan(another)
    }

    def one = new ComparableFoo(5)
    def another = new ComparableFoo(-5)

    assertEquals(10, comparator.compare(one, another))
    assertEquals(0, comparator.compare(one, one))
    assertEquals(-10, comparator.compare(another, one))

  }

  public void testClosureComparatorForNumericTypes() {

    def comparator = new ClosureComparator() { one, another ->
      one - another
    }

    assertEquals(1, comparator.compare(Integer.MAX_VALUE, Integer.MAX_VALUE-1))
    assertEquals(0, comparator.compare(Double.MIN_VALUE, Double.MIN_VALUE))
    assertEquals(-1, comparator.compare(Long.MIN_VALUE, Long.MIN_VALUE+1))
  }

}

class ComparableFoo {
  long value

  public ComparableFoo(long theValue) {
    this.value = theValue
  }

  def greaterThan(anotherFoo) {
    return (this.value - anotherFoo.value)
  }
}

